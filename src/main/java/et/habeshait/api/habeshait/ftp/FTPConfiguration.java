package et.habeshait.api.habeshait.ftp;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.ftp.outbound.FtpMessageHandler;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Configuration
public class FTPConfiguration {
	
	@Value("${ftp.host}")
	private String ftpHost;
	
	@Value("${ftp.port}")
	private String ftpPort;
	
	@Value("${ftp.user}")
	private String ftpUser;
	
	@Value("${ftp.password}")
	private String ftpPassword;
	
	@Bean
	public DefaultFtpSessionFactory sf() {
		DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
		sf.setHost(ftpHost);
		sf.setPort(Integer.parseInt(ftpPort));
		sf.setUsername(ftpUser);
		sf.setPassword(ftpPassword);
		sf.setClientMode(2);
		return sf;
	}
	
	@ServiceActivator(inputChannel = "ftpPut")
	@Bean
	public MessageHandler uploadFile() {
		ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
		
		FtpMessageHandler handler = new FtpMessageHandler (sf());
		handler.setLoggingEnabled(true);
		handler.setAutoCreateDirectory(false);
		handler.setRemoteDirectoryExpression(EXPRESSION_PARSER.parseExpression("headers['path']"));
	    handler.setFileNameGenerator(new FileNameGenerator() {
	        @Override
	        public String generateFileName(Message<?> message) {
	            return (String) message.getHeaders().get("filename");
	        }
	    });
	    return handler;
	}

	@MessagingGateway
	public interface Gate {
					     
	     @Gateway(requestChannel = "ftpPut")
	     void sendToFtp(@Payload byte[] file, @Header("filename") String filename, @Header("path") String path);

	}
}