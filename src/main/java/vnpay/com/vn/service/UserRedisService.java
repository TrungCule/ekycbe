package vnpay.com.vn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import vnpay.com.vn.domain.User;
import vnpay.com.vn.repository.RedisRepositoryImpl;

@Service
public class UserRedisService {

    private final RedisRepositoryImpl redisRepositoryImpl;

    public UserRedisService(RedisRepositoryImpl redisRepositoryImpl) {
        this.redisRepositoryImpl = redisRepositoryImpl;
    }

    public User findUserByUserLogin(String username) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        Object object = redisRepositoryImpl.findUserByUserLogin(username);
        if(!ObjectUtils.isEmpty(object)) {
            return objectMapper.readValue(object.toString(), User.class);
        }
        return null;
    }

    public void saveUserIntoRedis(User user) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        redisRepositoryImpl.setUserInfo(user);
    }

    public void deleteUserRedis(String userLogin) {
        redisRepositoryImpl.clearCache(userLogin);
    }
}
