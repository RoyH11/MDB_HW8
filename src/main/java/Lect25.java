import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class Lect25 {
    public static void main(String[] args) {
        // Connect to the local Redis server
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            // Begin a transaction
            Transaction transaction = jedis.multi();

            // Insert a value
            transaction.set("exampleKey", "1");

            System.out.println("Initial value: " + 1);

            // Increment the value
            Response<Long> incrementedValue = transaction.incr("exampleKey");

            // Execute the transaction
            transaction.exec();

            // Get the result of the increment operation
            Long result = incrementedValue.get();
            System.out.println("Result of increment: " + result);

            // Get the final value of the key
            String finalValue = jedis.get("exampleKey");
            System.out.println("Final value: " + finalValue);
        } catch (Exception e) {
            // Handle exceptions appropriately
            e.printStackTrace();
        }
    }
}