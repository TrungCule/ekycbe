package vnpay.com.vn.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vnpay.com.vn.domain.User;

@Repository
public class RedisRepositoryImpl {
    long expirationMinutes = 5;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void clearCache(String username) {
        redisTemplate.delete(username);
    }

    public void setUserInfo(User user) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        redisTemplate.opsForValue().set(user.getLogin(), objectMapper.writeValueAsString(user));
        redisTemplate.expire(user.getLogin(), expirationMinutes, java.util.concurrent.TimeUnit.MINUTES);
    }

    public Object findUserByUserLogin(String userLogin) {
        return redisTemplate.opsForValue().get(userLogin);
    }
}
