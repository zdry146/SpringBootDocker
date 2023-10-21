package social.server;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication(exclude = { MongoAutoConfiguration.class})
@RestController
public class SocialService {
	static final String version = "0.0.1";
	Logger logger;

	String URI = "mongodb://mike-virtual-machine1:27017,mike-virtual-machine2:27017,mike-virtual-machine3:27017/?replicaSet=rs0&retryWrites=true&w=majority&wtimeoutMS=3000&readConcernLevel=majority";
	//String URI = "mongodb://mongo1.external-apps.svc.cluster.local:27017,mongo2.external-apps.svc.cluster.local:27017,mongo3.external-apps.svc.cluster.local:27017/?replicaSet=rs0&retryWrites=true&w=majority&wtimeoutMS=3000&readConcernLevel=majority";

	MongoClient mongoClient = new MongoClient(new MongoClientURI(URI));
	APIRoutes userRoutes = new APIRoutes(mongoClient);
	public static void main(String[] args) {
		SpringApplication.run(SocialService.class, args);
	}
	public SocialService() {
		logger = LoggerFactory.getLogger(SocialService.class);
		logger.info(version);

		StartWorkers(mongoClient);

	}
	@GetMapping("/liveness")
	public String liveness(HttpServletResponse res) {
		return userRoutes.liveness(res);
	}

	//Create a new user
	@PostMapping("/users")
	public String getUser(@RequestBody String body, HttpServletResponse res) {
		return userRoutes.createUser(body, res);
	}

	//Fetch some basic info about a user (cannonical username, date joined)
	@GetMapping("/users/{userName}/profile")
	public String getUserProfile(@PathVariable String userName, HttpServletResponse res) {
		return userRoutes.getUserProfile(userName,res);
	}

	//Add a new follower to a User
	@PutMapping("/users/{userName}/followers/{followerName}")
	public String followUser(@PathVariable String userName, @PathVariable String followerName, HttpServletResponse res) {
		return userRoutes.followUser(userName, followerName, res);
	}
	//Remove a follower from a User
	@DeleteMapping("/users/{userName}/followers/{followerName}")
	public String unFollowUser(@PathVariable String userName, @PathVariable String followerName, HttpServletResponse res) {
		return userRoutes.unFollowUser(userName, followerName, res);
	}
	//Get a list of followers for a user
	@GetMapping("/users/{userName}/followers")
	public String unFollowUser(@PathVariable String userName, HttpServletResponse res) {
		return userRoutes.getFollowers(userName, res);
	}
	//Get a count of followers for a user (may be cached)
	@GetMapping("/users/{userName}/followers_count")
	public String getFollowersCount(@PathVariable String userName, HttpServletResponse res) {
		return userRoutes.getFollowersCount(userName, res);
	}
	//Get a list of those a user follows
	@GetMapping("/users/{userName}/following")
	public String getFollowing(@PathVariable String userName, HttpServletResponse res) {
		return userRoutes.getFollowing(userName, res);
	}
	//Get  a   count of how many a user follows
	@GetMapping("/users/{userName}/following_count")
	public String getFollowingCount(@PathVariable String userName, HttpServletResponse res) {
		return userRoutes.getFollowingCount(userName, res);
	}
	//Get the latest page of a users feed
	@GetMapping("/users/{userName}/feed")
	public String getFeed(@PathVariable String userName, HttpServletResponse res) {
		return userRoutes.getFeed(userName, res);
	}

	//Get the latest page of a users feed with pagenation
	@GetMapping("/users/{userName}/feedbefore/{from}")
	public String getFeedBefore(@PathVariable String userName,@PathVariable String from, HttpServletResponse res) {
		return userRoutes.getFeedBefore(userName, from, res);
	}
	//Post a new item to followers
	@PostMapping("/users/{userName}/posts")
	public String newPost(@PathVariable String userName, @RequestBody String body, HttpServletResponse res) {
		return userRoutes.newPost(userName,body, res);
	}
	//Get all posts by a user
	@GetMapping("/users/{userName}/posts")
	public String getPosts(@PathVariable String userName, HttpServletResponse res) {
		return userRoutes.getPosts(userName, res);
	}
	//Get all posts count by a user
	@GetMapping("/users/{userName}/posts_count")
	public String getPostsCount(@PathVariable String userName, HttpServletResponse res) {
		return userRoutes.getPostsCount(userName, res);
	}

	//Delete a post previously sent
	@DeleteMapping("/users/{userName}/posts/{postId}")
	public String deletePost(@PathVariable String userName, @PathVariable String postId, HttpServletResponse res) {
		return userRoutes.deletePost(userName, postId, res);
	}

	//Change nickname
	@PutMapping("/users/{userName}/nickname/{nickName}")
	public String changeNickname(@PathVariable String userName, @PathVariable String nickName, HttpServletResponse res) {
		return userRoutes.changeNickname(userName, nickName, res);
	}


	private static void StartWorkers(MongoClient mongoClient)
	{
		int nThreads = 1; //How many workers
		ExecutorService simexec = Executors.newFixedThreadPool(nThreads);
		for (int workerno = 0; workerno < nThreads; workerno++) {
			simexec.execute(new WorkerTask(workerno,mongoClient));
		}
		simexec.shutdown();
	}

}
