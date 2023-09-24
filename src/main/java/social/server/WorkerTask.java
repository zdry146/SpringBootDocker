package social_server;

import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class WorkerTask implements Runnable {

    Logger logger;
    int threadId;
    MongoClient mongoClient;

    WorkerTask(int threadid, MongoClient mongoClient) {
        logger = LoggerFactory.getLogger(WorkerTask.class);
        this.threadId = threadid;
        this.mongoClient = mongoClient;
    }

    boolean distributePost() {

        PostsDAL post = new PostsDAL(mongoClient);
        ObjectId me = new ObjectId();

        if (post.ClaimUndistributedPost(me)) {
            /*
             * We could have clever code to decide WHO to send it to here so it's not in the
             * DAL
             */
            ArrayList<String> followers = post.getPoster().getFollowers();
            for (String follower : followers) {
                logger.info("Sending post to {}", follower);
                post.fanOutToFollower(follower);
            }
            post.MarkPostDistributed(me);
            return true;
        }

        return false;
    }

    public void run() {
        logger.info("Thread {} has started.", threadId);

        // Slight risk of busy waiting so sleep if we don't find any
        // Alternatively we could, and probably should use a change stream
        // rather than poll

        while (true) {
            if (distributePost() == false) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } // Poll every 5s when queue is empty
            }
        }
    }

}