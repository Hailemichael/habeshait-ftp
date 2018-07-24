package et.habeshait.api.habeshait.ftp.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import et.habeshait.api.habeshait.ftp.config.FTPConfiguration.Gate;

@Controller
@RequestMapping("/")
public class FTPClientController {
	private static final Logger logger = LoggerFactory.getLogger(FTPClientController.class);
	
	@Autowired
	private Gate gate;
	
	@RequestMapping(value="/ftp/upload", method=RequestMethod.POST, headers = "content-type=multipart/form-data")
    public @ResponseBody ResponseEntity<?> ftpUpload(@RequestParam(value="file", required=true) MultipartFile file, @RequestParam(value="path", required=true) String path) {
		HashMap<String, String> map = new HashMap<String, String> ();
		if(file.isEmpty()) {//error codes to be added
			logger.error("The file is empty!");
			map.put("error", "The file is empty!");
			return new ResponseEntity<> (map, HttpStatus.BAD_REQUEST);
		}
		byte[] toUpload = null;
		try {
			toUpload = file.getBytes();
		} catch (IOException e) {
			logger.error(e.getMessage(), e.getCause());
			map.put("error", e.getMessage());
			return new ResponseEntity<> (map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String fileName = (new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSZ")).format(new Date()) + "_" + file.getOriginalFilename();
		gate.sendToFtp(toUpload, fileName, path);
		
		map.put("imageUrl", "http://habeshait.com/MemePics" + path + "/" + fileName);
		return new ResponseEntity<> (map, HttpStatus.OK);
    }
}
