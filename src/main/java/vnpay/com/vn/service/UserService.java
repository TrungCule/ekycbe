package vnpay.com.vn.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tech.jhipster.security.RandomUtil;
import vnpay.com.vn.domain.Authority;
import vnpay.com.vn.domain.User;
import vnpay.com.vn.repository.AuthorityRepository;
import vnpay.com.vn.repository.UserRepository;
import vnpay.com.vn.security.AuthoritiesConstants;
import vnpay.com.vn.security.SecurityUtils;
import vnpay.com.vn.security.jwt.TokenProvider;
import vnpay.com.vn.service.Utils.Utils;
import vnpay.com.vn.service.dto.*;
import vnpay.com.vn.service.util.ExcelConstant;
import vnpay.com.vn.service.util.FastExcelUtils;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;
    private final TokenProvider tokenProvider;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.tokenProvider = tokenProvider;
    }


    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
            .findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                return user;
            });
    }

    public User registerUser(AdminUserDTO userDTO, String password) {
        userRepository
            .findOneByLogin(userDTO.getLogin().toLowerCase())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new UsernameAlreadyUsedException();
                }
            });
        userRepository
            .findOneByEmailIgnoreCase(userDTO.getEmail())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new EmailAlreadyUsedException();
                }
            });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        newUser.setPhoneNumber(userDTO.getPhoneNumber());
        newUser.setAddress(userDTO.getAddress());
        newUser.setDateOfBirth(userDTO.getDateOfBirth());
        newUser.setRetypePassword(userDTO.getRetypePassword());
        // new user is not active
        newUser.setActivated(true);
        // new user gets registration key
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        return true;
    }

    public User createUser(AdminUserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setRetypePassword(userDTO.getRetypePassword());
        user.setAddress(userDTO.getAddress());
        user.setDateOfBirth(userDTO.getDateOfBirth());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO
                .getAuthorities()
                .stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return Optional
            .of(userRepository.findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setActivated(userDTO.isActivated());
                user.setRetypePassword(userDTO.getRetypePassword());
                user.setAddress(userDTO.getAddress());
                user.setDateOfBirth(userDTO.getDateOfBirth());
                user.setPhoneNumber(userDTO.getPhoneNumber());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO
                    .getAuthorities()
                    .stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                userRepository.save(user);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(AdminUserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository
            .findOneByLogin(login)
            .ifPresent(user -> {
                userRepository.delete(user);
                log.debug("Deleted User: {}", user);
            });
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     */
    public void updateUserNormal(AdminUserDTO userDTO) {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setAddress(userDTO.getAddress());
                user.setPhoneNumber(userDTO.getPhoneNumber());
                user.setDateOfBirth(userDTO.getDateOfBirth());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                userRepository.save(user);
                log.debug("Changed Information for User: {}", user);
            });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                log.debug("Changed password for User: {}", user);
            });
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                log.debug("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     *
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    public Optional<User> getUserByUserName(String userName) {
        return userRepository.findOneByLogin(userName);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getUsersByTextSearch(Pageable pageable, String textSearch) {
        if (!StringUtils.isEmpty(textSearch)) {
            return userRepository.findUsersByTextSearch("%" + textSearch + "%", pageable).map(AdminUserDTO::new);
        }
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    public User registerUserTest(AdminUserDTO userDTO, String password) {
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        newUser.setPhoneNumber(userDTO.getPhoneNumber());
        newUser.setAddress(userDTO.getAddress());
        newUser.setDateOfBirth(userDTO.getDateOfBirth());
        newUser.setRetypePassword(userDTO.getRetypePassword());
        // new user is not active
        newUser.setActivated(true);
        // new user gets registration key
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        return newUser;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                log.debug("Activated user: {}", user);
                return user;
            });
    }
    public byte[] getExcelFile(SearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPageNumber(), searchDTO.getPageSize());
        Page<User> usersPage = userRepository.findAllUsersByTextSearch("%" + searchDTO.getTextSearch() + "%", pageable);
        List<User> users = usersPage.getContent();
            if (users.size() == 0) {
                users.add(new User());
                users.add(new User());
                users.add(new User());
            }

           ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                List<CellStyle> cellStyleList = FastExcelUtils.getCellStyleList(workbook);
                SXSSFSheet sheet = workbook.createSheet("Sheet 1");

                List<HeaderConfig> headerConfig = ExcelConstant.AccountingObject.HeaderConfig;
                List<FieldConfig> fieldConfig = ExcelConstant.AccountingObject.FieldConfig;

                // Set Thông tin cơ cấu tổ chức, tên mẫu
                FastExcelUtils.SetHeaderExcel(
                    sheet,
                    fieldConfig.size(),
                    cellStyleList,
                    "DANH SÁCH NGƯỜI DÙNG EKYC"
                );
//                set peried
                Date date = new Date();
                LocalDate localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                String period = Utils.getPeriod(localDate, localDate);
                Row rowDate = sheet.createRow(6);
                Cell cellRowDate = rowDate.createCell(0);
                cellRowDate.setCellStyle(cellStyleList.get(2));
                cellRowDate.setCellValue(period);
                CellUtil.setAlignment(cellRowDate, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
                sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, fieldConfig.size() - 1));

                int startRow = 12;

                FastExcelUtils.genBodyDynamicReportExcel(sheet, fieldConfig, headerConfig, users, startRow, cellStyleList);

                startRow = startRow + users.size();
                sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 0, 2));
//                Row rowTotal = sheet.getRow(startRow);
//                CellStyle rightAligned = cellStyleList.get(1);
//                CellStyle cellStyleNumber = cellStyleList.get(6);
//                rightAligned.setAlignment(HorizontalAlignment.RIGHT);
//                for (int i = 0; i < 8; i++) {
//                    Cell cellTotal = rowTotal.getCell(i);
//                    cellTotal.setCellStyle(cellStyleNumber);
//                    if (i == 0) {
//                        cellTotal.setCellStyle(rightAligned);
//                        cellTotal.setCellStyle(cellStyleList.get(1));
//                        cellTotal.setCellValue("Tổng cộng");
//                        CellUtil.setAlignment(cellTotal, org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
//                    }
//                }

                FastExcelUtils.SetFooterExcel(sheet, startRow + 1, fieldConfig.size(), cellStyleList);
                sheet.trackAllColumnsForAutoSizing();

                for (int c = 0; c < fieldConfig.size(); c++) {
                    sheet.autoSizeColumn(c, true);
                    if (sheet.getColumnWidth(c) > (45 * 256)) {
                        sheet.setColumnWidth(c, 45 * 256);
                    } else if (sheet.getColumnWidth(c) < (8 * 256)) {
                        sheet.setColumnWidth(c, 8 * 256);
                    }
                }
                workbook.write(bos);
                workbook.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            return bos.toByteArray();
    }

}
