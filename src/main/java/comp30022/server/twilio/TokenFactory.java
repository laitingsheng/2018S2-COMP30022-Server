package comp30022.server.twilio;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.AccessToken.Builder;
import com.twilio.jwt.accesstoken.ChatGrant;
import com.twilio.jwt.accesstoken.Grant;

import java.util.HashMap;

import static comp30022.server.twilio.TwilioConstants.*;

public class TokenFactory {
    private static final HashMap<String, TokenGenerator> generators = new HashMap<>();

    static {
        generators.put("chat", (uid, device) -> {
            ChatGrant grant = new ChatGrant();
            grant.setServiceSid(TWILIO_CHAT_SERVICE_SID);
            grant.setPushCredentialSid(TWILIO_FIREBASE_PUSH_CREDENTIAL);
            grant.setEndpointId(TWILIO_APP_NAME + ":" + uid + ":" + device);

            return buildToken(grant, uid);
        });
        // TODO: Add video token generator
        // TODO: Add audio token generator
    }

    private static AccessToken buildToken(Grant grant, String uid) {
        return new Builder(TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET).identity(uid).grant(grant).build();
    }

    public static TokenResponse generateToken(String type, String uid, String device) {
        return new TokenResponse(uid, generators.get(type).generate(uid, device));
    }

    @FunctionalInterface
    private interface TokenGenerator {
        AccessToken generate(String uid, String device);
    }
}
