package ac.in.iiitd.fcs29;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//import javax.crypto.KeyGenerator;
//import java.util.Base64;

@SpringBootApplication
@EnableScheduling
public class Fcs29MvnApplication {

	public static void main(String[] args) {
		SpringApplication.run(Fcs29MvnApplication.class, args);
		System.out.println("Backend Started");
//		KeyGenerator keyGenerator = KeyGenerator.getInstance("ChaCha20");
//		keyGenerator.init(256);
//		byte[] key = keyGenerator.generateKey().getEncoded();
//
//		System.out.println("Generated Key (Base64): " + Base64.getEncoder().encodeToString(key));
//		System.out.println("Generated Key (Base64): " + Base64.getDecoder().decode("gxbTlRX3Kk5Vr0IzOrZvzCdK7WegcLbed8c/0BlQU/I=").length);
//		System.out.println("Generated Key (Base64): " + key.length);
//		System.out.println("Generated Key (Base64): " + key.toString());
	}

}
