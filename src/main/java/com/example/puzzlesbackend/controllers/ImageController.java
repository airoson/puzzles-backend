package com.example.puzzlesbackend.controllers;

import com.example.puzzlesbackend.services.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/image")
@Slf4j
public class ImageController {
    @Autowired
    private ImageService imageService;

    @GetMapping(produces="image/png")
    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost", "http://localhost:3001"}, allowCredentials = "true")
            public ResponseEntity<?> getImage(@RequestParam(name = "image_id") String imageId){
        byte[] data = imageService.findImageById(imageId);
        if(data != null){
            CacheControl cacheControl = CacheControl.maxAge(365, TimeUnit.DAYS).cachePrivate();
            return ResponseEntity.ok().cacheControl(cacheControl).body(data);
        }else{
            Map<String, String> message = new TreeMap<>();
            message.put("message", "image not found");
            return ResponseEntity.ok().body(message);
        }
    }

}
