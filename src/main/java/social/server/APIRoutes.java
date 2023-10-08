package social.server;

import com.mongodb.MongoClient;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

/* As a microservice we really want to be stateless so each of these should read/write to the DB*/
/* We are using Document here as a generic MAP<->JSON conversion - we coudl use Map<String,Object> */


public class APIRoutes {

    Logger logger;
    MongoClient mongoClient;

    // Define how to write JSON with types like Binary, GUID , Decimal128 and Date()
    // output as strings by default MongoDB preserves this type information outputting JSON.
    // We could use a standard JSON writer like GSON but MDB comes with one.

    JsonWriterSettings plainJSON = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED)
            .binaryConverter((value, writer) -> writer.writeString(Base64.getEncoder().encodeToString(value.getData())))
            .dateTimeConverter((value, writer) -> {
                ZonedDateTime zonedDateTime = Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC);
                writer.writeString(DateTimeFormatter.ISO_DATE_TIME.format(zonedDateTime));
            }).decimal128Converter((value, writer) -> writer.writeString(value.toString()))
            .objectIdConverter((value, writer) -> writer.writeString(value.toHexString()))
            .symbolConverter((value, writer) -> writer.writeString(value)).build();

    //So we can connect any DALs we use to the database
    APIRoutes(MongoClient mongoClient) {
        logger = LoggerFactory.getLogger(APIRoutes.class);
        this.mongoClient = mongoClient;
        // Force a connection test - will error out if it cannot
        mongoClient.getDatabase("any").runCommand(new Document("ping", 1));
    }

    // curl -X PUT http://localhost:4567/users/bob/followers/jim

    public String followUser(String star_name, String follower_name, HttpServletResponse res) {
        UserDAL follower;

        follower = new UserDAL(mongoClient, follower_name);
        if (follower.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", follower.getLastError()).toJson();
        }

        if (follower.followUser(star_name) == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", follower.getLastError()).toJson();
        }
        return new Document("ok", true).toJson();
    }

    // curl -X PUT http://localhost:4567/users/bob/followers/jim

    public String unFollowUser(String star_name, String follower_name, HttpServletResponse res) {
        UserDAL follower;

        follower = new UserDAL(mongoClient, follower_name);
        if (follower.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", follower.getLastError()).toJson();
        }

        if (follower.unFollowUser(star_name) == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", follower.getLastError()).toJson();
        }
        return new Document("ok", true).toJson();
    }

	/* Test with:
	 curl -d '{ "username": "bob" }' -X POST -H "Content-Type: app.ication/json" http://localhost:4567/users
	*/

    // Create a new User Record Object
    // {username: x}

    public String createUser(String body, HttpServletResponse res) {
        Document postdata;

        try {
            postdata = Document.parse(body);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            res.setStatus(400);
            return new Document("ok", false).append("error", e.getMessage()).append("src", body).toJson();
        }

        String username = postdata.getString("username");
        if (username == null) {
            res.setStatus(400);
            return new Document("ok", false).append("error", "username is required").toJson();
        }

        UserDAL newUser = new UserDAL(mongoClient);

        if (newUser.createUser(username) == false) {
            res.setStatus(409);
            return new Document("ok", false).append("error", newUser.getLastError()).toJson();
        }

        return new Document("ok", true).toJson();
    }

    // curl -X GET http://localhost:4567/users/bob/profile
    public String getUserProfile(String userName, HttpServletResponse res) {
        UserDAL user;

        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }
        // Construct a JSON Document to return in our API
        Document userForAPI = new Document("apiversion", "1.0");
        userForAPI.append("datecreated", user.getCreateDate());
        userForAPI.append("numposts", user.getPostCount());
        userForAPI.append("username", user.getCurrentName(user.getUsername()));
        return new Document("ok", true).append("user", userForAPI).toJson(plainJSON);

    }

    // curl -X GET http://localhost:4567/users/bob/followers
    public String getFollowers(String userName, HttpServletResponse res) {
        UserDAL user;

        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        ArrayList<String> followers = user.getFollowers();
        followers = user.getCurrentNames(followers);

        // Construct a JSON Document to return in our API
        Document userForAPI = new Document("apiversion", "1.0");
        userForAPI.append("username", user.getUsername());
        userForAPI.append("followers", followers);
        return new Document("ok", true).append("user", userForAPI).toJson(plainJSON);

    }

    // curl -X GET http://localhost:4567/users/bob/followerscount
    public String getFollowersCount(String userName, HttpServletResponse res) {
        UserDAL user;

        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        int count = user.getFollowersCount();

        // Construct a JSON Document to return in our API
        Document userForAPI = new Document("apiversion", "1.0");
        userForAPI.append("username", user.getUsername());
        userForAPI.append("followerscount", count);
        return new Document("ok", true).append("user", userForAPI).toJson(plainJSON);

    }

    public String getFollowing(String userName, HttpServletResponse res) {
        UserDAL user;

        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        ArrayList<String> following = user.getFollowing();
        following = user.getCurrentNames(following);
        if (following == null) {
            res.setStatus(400);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        // Construct a JSON Document to return in our API
        Document userForAPI = new Document("apiversion", "1.0");
        userForAPI.append("username", user.getUsername());
        userForAPI.append("following", following);
        return new Document("ok", true).append("user", userForAPI).toJson(plainJSON);

    }

    // curl -X GET http://localhost:4567/users/bob/followerscount
    public String getFollowingCount(String userName, HttpServletResponse res) {
        UserDAL user;

        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        int count = user.getFollowingCount();

        // Construct a JSON Document to return in our API
        Document userForAPI = new Document("apiversion", "1.0");
        userForAPI.append("username", user.getUsername());
        userForAPI.append("followingcount", count);
        return new Document("ok", true).append("user", userForAPI).toJson(plainJSON);

    }

    //Post a document
    public String newPost(String userName, String body, HttpServletResponse res) {
        UserDAL user;
        Document postdata;

        try {
            postdata = Document.parse(body);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            res.setStatus(400);
            return new Document("ok", false).append("error", e.getMessage()).toJson();
        }

        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        String postText = postdata.getString("text");
        if (postText == null) {
            res.setStatus(400);
            return new Document("ok", false).append("error", "No post text").toJson();
        }

        PostsDAL post = new PostsDAL(mongoClient, user, postText, new Date());
        if (post.postToFollowers() == false) {
            res.setStatus(403);
            return new Document("ok", false).append("error", post.getLastError()).toJson();
        }
        return new Document("ok", true).toJson();
    }

    //Get all posts by a user

    public String getPosts(String userName, HttpServletResponse res) {
        UserDAL user;

        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        ArrayList<PostsDAL> posts = user.getPosts();
        ArrayList<Document> postsForAPI = new ArrayList<Document>();
        for (PostsDAL p : posts) {
            Document postForAPI = new Document("postid", p.getId().toHexString()).append("text", p.getText());

            postForAPI.append("postDate", p.getPostDate()).append("poster", user.getCurrentName(p.getPostername()));
            postsForAPI.add(postForAPI);
        }
        return new Document("ok", true).append("posts", postsForAPI).toJson();
    }

    //Get all posts count by a user

    public String getPostsCount(String userName, HttpServletResponse res) {
        UserDAL user;

        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        Integer postCount = user.getPostCount();

        return new Document("ok", true).append("postsCount", postCount).toJson();
    }


    //Underlying implemenation of feed adding default from of 0
    String getFeed(String userName, HttpServletResponse res) {
        UserDAL user;
        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        ArrayList<PostsDAL> posts = user.getFeed();
        ArrayList<Document> postsForAPI = new ArrayList<Document>();
        Document cache = new Document(); // Minimise DB lookups

        for (PostsDAL p : posts) {
            Document postForAPI = new Document("postid", p.getId().toHexString()).append("text", p.getText());
            String currentname = cache.getString(p.getPostername());
            if (currentname == null) {
                currentname = user.getCurrentName(p.getPostername());
                cache.put(p.getPostername(), currentname);
            }
            postForAPI.append("postDate", p.getPostDate()).append("poster", currentname);
            postsForAPI.add(postForAPI);
        }
        return new Document("ok", true).append("feed", postsForAPI).toJson();
    }

    public String getFeedBefore(String userName, String from, HttpServletResponse res) {
        return getFeedInternal(userName, from, res);
    }

    String getFeedInternal(String userName, String feedfrom, HttpServletResponse res) {
        UserDAL user;
        user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        ArrayList<PostsDAL> posts = user.getFeed(feedfrom);
        ArrayList<Document> postsForAPI = new ArrayList<Document>();
        Document cache = new Document(); // Minimise DB lookups
        for (PostsDAL p : posts) {
            Document postForAPI = new Document("postid", p.getId().toHexString()).append("text", p.getText());
            String currentname = cache.getString(p.getPostername());
            if (currentname == null) {
                currentname = user.getCurrentName(p.getPostername());
                cache.put(p.getPostername(), currentname);
            }
            postForAPI.append("postDate", p.getPostDate()).append("poster", currentname);
            postsForAPI.add(postForAPI);
        }
        return new Document("ok", true).append("feed", postsForAPI).toJson();
    }
    //Delete a post by a user

    public String deletePost(String userName, String postId, HttpServletResponse res) {
        UserDAL user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        PostsDAL p = new PostsDAL(mongoClient);
        if (p.DeletePost(user, postId) == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", "Not Found:").toJson();
        }

        return new Document("ok", true).toJson();
    }

    //Set the name a user wants to by seen as

    public String changeNickname(String userName, String nickName, HttpServletResponse res) {
        UserDAL user = new UserDAL(mongoClient, userName);

        if (user.isPopulated() == false) {
            res.setStatus(404);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }


        //Add when we implement change nickname
        if (user.changeNickname(nickName) == false) {
            res.setStatus(400);
            return new Document("ok", false).append("error", user.getLastError()).toJson();
        }

        return new Document("ok", true).toJson();
    }


    public String liveness(HttpServletResponse res) {
        return new Document("ok", true).toJson();
    }
}
