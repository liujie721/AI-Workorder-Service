import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TempController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/gen-pwd")
    public String genPwd(@RequestParam String pwd) {
        return passwordEncoder.encode(pwd);
    }
}