package vnpay.com.vn.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final long tokenValidityInMilliseconds = 6000;

    public static final long tokenValidityInMillisecondsForRefresh = 12000;

    public static final String SECRET = "bezKoderSecretKeyFSDGDAFSFDAFFEWRFDCdcsfDFSAFDs";

    public static final String AUTHORITIES_KEY = "auth";

    private AuthoritiesConstants() {}
}
