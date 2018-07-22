package et.habeshait.api.habeshait.ftp;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway.Option;
import org.springframework.integration.ftp.gateway.FtpOutboundGateway;
import org.springframework.integration.ftp.outbound.FtpMessageHandler;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Configuration
public class FTPConfiguration {
	
	@Bean
	public DefaultFtpSessionFactory sf() {
		DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
		sf.setHost("ftp.habeshait.com");
		sf.setPort(21);
		sf.setUsername("haile@habeshait.com");
		sf.setPassword("Abbhst@1985");
		return sf;
	}
	
	@ServiceActivator(inputChannel = "ftpLS")
	@Bean
	public FtpOutboundGateway getGW() {
		FtpOutboundGateway gateway = new FtpOutboundGateway(sf(), "ls", "payload");
		gateway.setOption(Option.NAME_ONLY);
		gateway.setOutputChannelName("results");
		return gateway;
	}
	
	@ServiceActivator(inputChannel = "ftpPut")
	@Bean
	public MessageHandler uploadFile() {
		ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
		
		FtpMessageHandler handler = new FtpMessageHandler (sf());
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

	@Bean
	public MessageChannel results() {
		DirectChannel channel = new DirectChannel();
		channel.addInterceptor(tap());
		return channel;
	}

	@Bean
	public WireTap tap() {
		return new WireTap("logging");
	}

	@ServiceActivator(inputChannel = "logging")
	@Bean
	public LoggingHandler logger() {
		LoggingHandler logger = new LoggingHandler(Level.INFO);
		logger.setLogExpressionString("'Files:' + payload");
		return logger;
	}

	@MessagingGateway
	public interface Gate {

		@SuppressWarnings("rawtypes")
		@Gateway(requestChannel = "ftpLS", replyChannel = "results")
		List list(String directory);
					     
	     @Gateway(requestChannel = "ftpPut")
	     void sendToFtp(@Payload byte[] file, @Header("filename") String filename, @Header("path") String path);

	}
}