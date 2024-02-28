package com.example.puzzlesbackend.services;

import com.example.puzzlesbackend.dto.ImageCropParams;
import com.example.puzzlesbackend.entities.Puzzle;
import com.example.puzzlesbackend.entities.PuzzleImage;
import com.example.puzzlesbackend.repositories.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    @Value("${game.male-mask}")
    private String maleMaskFile;

    @Value("${game.female-mask}")
    private String femaleMaskFile;

    @Value("${game.female-mask-v}")
    private String femaleMaskVFile;

    @Value("${game.male-mask-v}")
    private String maleMaskVFile;

    public ImageCropParams getCropPuzzleParams(int puzzlesCount, int imageWidth, int imageHeight){
        int maxSize = 0, correspondingWidth = 0, correspondingHeight = 0;
        for(int curCount = 1; curCount < (int)Math.sqrt(puzzlesCount); curCount++){
            if(puzzlesCount % curCount != 0)continue;
            int curWidth, curHeight, curCountX, curCountY;
            if(imageWidth > imageHeight){
                curCountX = puzzlesCount / curCount;
                curCountY = curCount;
            }else{
                curCountX = curCount;
                curCountY = puzzlesCount / curCount;
            }
            curWidth = imageWidth / curCountX;
            curHeight = imageHeight / curCountY;
            int curSize = Math.min(curWidth, curHeight);
            if(curSize > maxSize){
                maxSize = curSize;
                correspondingHeight = curCountY;
                correspondingWidth = curCountX;
            }
        }
        return new ImageCropParams(correspondingWidth, correspondingHeight, maxSize);
    }

    private BufferedImage drawARGBImage(Image image, int width, int height){
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var g = res.getGraphics();
        g.drawImage(image, 0, 0, width, height,null);
        g.dispose();
        return res;
    }

    private void addAlpha(int[] mask, int[] image, int shift){
        for(int i = 0; i < Math.min(mask.length, image.length); i++){
            int color = image[i] & 0x00ffffff;
            int alpha = ((mask[i] << shift) & 0xff000000) ^ 0xff000000;
            image[i] = color | alpha;
        }
    }

    public List<BufferedImage> cutImage(BufferedImage source, ImageCropParams params, List<Puzzle> layout){
        BufferedImage image = drawARGBImage(source, source.getWidth(), source.getHeight());
        try{
            Image maleMaskNotScaled = ImageIO.read(new FileInputStream(System.getProperty("user.dir") + "/" + maleMaskFile));
            Image femaleMaskNotScaled = ImageIO.read(new FileInputStream(System.getProperty("user.dir") + "/" + femaleMaskFile));
            Image maleMaskVNotScaled = ImageIO.read(new FileInputStream(System.getProperty("user.dir") + "/" + maleMaskVFile));
            Image femaleMaskVNotScaled = ImageIO.read(new FileInputStream(System.getProperty("user.dir") + "/" + femaleMaskVFile));
            int scaledMaskWidth = (int)(maleMaskNotScaled.getWidth(null) * (params.puzzleSize() / (double)maleMaskNotScaled.getHeight(null)));
            int scaledMaskHeight = params.puzzleSize();
            BufferedImage maleMask = drawARGBImage(maleMaskNotScaled, scaledMaskWidth, scaledMaskHeight);
            BufferedImage femaleMask = drawARGBImage(femaleMaskNotScaled, scaledMaskWidth, scaledMaskHeight);
            BufferedImage maleMaskV = drawARGBImage(maleMaskVNotScaled, scaledMaskHeight, scaledMaskWidth);
            BufferedImage femaleMaskV = drawARGBImage(femaleMaskVNotScaled, scaledMaskHeight, scaledMaskWidth);
            List<BufferedImage> results = new LinkedList<>();
            int offsetX = (image.getWidth() - params.width() * params.puzzleSize()) / 2;
            int offsetY = (image.getHeight() - params.height() * params.puzzleSize()) / 2;
            var puzzleIter = layout.iterator();
            int count = 1;
            for(int y = 0; y < params.height(); y++){
                for(int x = 0; x < params.width(); x++){
                    BufferedImage crop = image.getSubimage(offsetX + x * params.puzzleSize(), offsetY + y * params.puzzleSize(), params.puzzleSize(), params.puzzleSize());
                    int puzzleImgSize = params.puzzleSize() + 2 * scaledMaskWidth;
                    BufferedImage puzzle = new BufferedImage(puzzleImgSize, puzzleImgSize, BufferedImage.TYPE_INT_ARGB);
                    var graphic = puzzle.getGraphics();
                    graphic.setColor(new Color(0,0,0,0));
                    graphic.fillRect(0, 0, puzzleImgSize, puzzleImgSize);
                    graphic.drawImage(crop, scaledMaskWidth, scaledMaskWidth, null);
                    int connectors = puzzleIter.next().getConnectors();
                    for(int i = 0; i < 4; i++){
                        int type = (connectors & (0b11 << (i * 2))) >> (i * 2);
                        BufferedImage connectorImg = null;
                        if(type == 1){
                            if(i == 0){
                                var tempImg = image.getSubimage(offsetX + x * params.puzzleSize(), offsetY + y * params.puzzleSize() - scaledMaskWidth, scaledMaskHeight, scaledMaskWidth);
                                connectorImg = new BufferedImage(tempImg.getWidth(), tempImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
                                connectorImg.getGraphics().drawImage(tempImg, 0, 0, null);
                                int[] imagePixels = connectorImg.getRGB(0, 0, connectorImg.getWidth(), connectorImg.getHeight(), null, 0, connectorImg.getWidth());
                                int[] maskPixels = maleMaskV.getRGB(0, 0, scaledMaskHeight, scaledMaskWidth, null, 0, scaledMaskHeight);
                                addAlpha(maskPixels, imagePixels, 16);
                                connectorImg.setRGB(0, 0, scaledMaskHeight, scaledMaskWidth, imagePixels, 0, scaledMaskHeight);
                                graphic.drawImage(connectorImg, scaledMaskWidth, 0, null);
                                log.info("Puzzle {}, side {}, pos {} {}", count, i, scaledMaskWidth, 0);
                            }else if(i == 1){
                                var tempImg = image.getSubimage(offsetX + (x + 1) * params.puzzleSize(), offsetY + y * params.puzzleSize(), scaledMaskWidth, scaledMaskHeight );
                                connectorImg = new BufferedImage(tempImg.getWidth(), tempImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
                                connectorImg.getGraphics().drawImage(tempImg, 0, 0, null);
                                int[] imagePixels = connectorImg.getRGB(0, 0, connectorImg.getWidth(), connectorImg.getHeight(), null, 0, connectorImg.getWidth());
                                int[] maskPixels = maleMask.getRGB(0, 0, scaledMaskWidth, scaledMaskHeight, null, 0, scaledMaskWidth);
                                addAlpha(maskPixels, imagePixels, 16);
                                connectorImg.setRGB(0, 0, scaledMaskWidth, scaledMaskHeight, imagePixels, 0, scaledMaskWidth);
                                graphic.drawImage(connectorImg, scaledMaskWidth + params.puzzleSize(), scaledMaskWidth, null);
                            }else if(i == 2){
                                var tempImg = image.getSubimage(offsetX + x * params.puzzleSize(), offsetY + (y + 1) * params.puzzleSize(), scaledMaskHeight, scaledMaskWidth);
                                connectorImg = new BufferedImage(tempImg.getWidth(), tempImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
                                connectorImg.getGraphics().drawImage(tempImg, 0, 0, null);
                                int[] imagePixels = connectorImg.getRGB(0, 0, connectorImg.getWidth(), connectorImg.getHeight(), null, 0, connectorImg.getWidth());
                                int[] maskPixels = maleMaskV.getRGB(0, 0, scaledMaskHeight, scaledMaskWidth, null, 0, scaledMaskHeight);
                                addAlpha(maskPixels, imagePixels, 8);
                                connectorImg.setRGB(0, 0, scaledMaskHeight, scaledMaskWidth, imagePixels, 0, scaledMaskHeight);
                                graphic.drawImage(connectorImg, scaledMaskWidth, scaledMaskWidth + params.puzzleSize(), null);
                            }else{
                                var tempImg= image.getSubimage(offsetX + x * params.puzzleSize() - scaledMaskWidth, offsetY + y * params.puzzleSize(), scaledMaskWidth, scaledMaskHeight);
                                connectorImg = new BufferedImage(tempImg.getWidth(), tempImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
                                connectorImg.getGraphics().drawImage(tempImg, 0, 0, null);
                                int[] imagePixels = connectorImg.getRGB(0, 0, connectorImg.getWidth(), connectorImg.getHeight(), null, 0, connectorImg.getWidth());
                                int[] maskPixels = maleMask.getRGB(0, 0, scaledMaskWidth, scaledMaskHeight, null, 0, scaledMaskWidth);
                                addAlpha(maskPixels, imagePixels, 8);
                                connectorImg.setRGB(0, 0, scaledMaskWidth, scaledMaskHeight, imagePixels, 0, scaledMaskWidth);
                                graphic.drawImage(connectorImg, 0, scaledMaskWidth, null);
                            }
                        }else if(type == 2){
                            int shift;
                            BufferedImage cMask;
                            if(i == 0){
                                connectorImg = puzzle.getSubimage(scaledMaskWidth, scaledMaskWidth, scaledMaskHeight, scaledMaskWidth);
                                cMask = femaleMaskV;
                                shift = 8;
                            }else if(i == 1){
                                connectorImg = puzzle.getSubimage(params.puzzleSize(), scaledMaskWidth, scaledMaskWidth, scaledMaskHeight);
                                cMask = femaleMask;
                                shift = 8;
                            }else if(i == 2){
                                connectorImg = puzzle.getSubimage(scaledMaskWidth, params.puzzleSize(), scaledMaskHeight, scaledMaskWidth);
                                cMask = femaleMaskV;
                                shift = 16;
                            }else{
                                connectorImg = puzzle.getSubimage(scaledMaskWidth, scaledMaskWidth, scaledMaskWidth, scaledMaskHeight);
                                cMask = femaleMask;
                                shift = 16;
                            }
                            int[] imagePixels = connectorImg.getRGB(0, 0, connectorImg.getWidth(), connectorImg.getHeight(), null, 0, connectorImg.getWidth());
                            int[] maskPixels = cMask.getRGB(0, 0, cMask.getWidth(), cMask.getHeight(), null, 0, cMask.getWidth());
                            addAlpha(maskPixels, imagePixels, shift);
                            connectorImg.setRGB(0, 0, connectorImg.getWidth(), connectorImg.getHeight(), imagePixels, 0, connectorImg.getWidth());
                        }
                    }
                    count++;
                    graphic.dispose();
                    results.add(puzzle);
                }
            }
            return results;
        }catch(IOException e){
            log.error(e.getMessage());
            return List.of();
        }
    }

    public void addImage(byte[] imageData, String originalName, String id){
        PuzzleImage image = new PuzzleImage();
        image.setData(imageData);
        image.setOriginalName(originalName);
        image.setId(id);
        imageRepository.save(image);
    }

    public byte[] findImageById(String imageId){
        PuzzleImage image = imageRepository.findById(imageId).orElse(null);
        if(image == null){
            return null;
        }
        return image.getData();
    }
}
