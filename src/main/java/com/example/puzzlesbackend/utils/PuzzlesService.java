package com.example.puzzlesbackend.utils;

import com.example.puzzlesbackend.dto.FieldParams;
import com.example.puzzlesbackend.dto.ImageCropParams;
import com.example.puzzlesbackend.entities.Puzzle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class PuzzlesService {

    @Value("${game.puzzle-grid-width}")
    private int puzzlesGridWidth;

    private int getIdByXY(int x, int y, int width){
        return y * width + x;
    }

    public List<Puzzle> generateLayout(int width, int height){
        List<Puzzle> puzzles = new LinkedList<>();
        int[] setTypes = new int[width];
        int id = 0;
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tempType = setTypes[x];
                Puzzle puzzle = new Puzzle();
                List<Integer> neighbors = new LinkedList<>();
                //set random connector for right, if it is not end of the line
                if(x != width - 1){
                    int rightConnector = (int)(Math.random() * 2) + 1;
                    tempType |= rightConnector << 2;
                    rightConnector ^= 0b11;
                    setTypes[x + 1] |= rightConnector << 6;
                    neighbors.add(getIdByXY(x + 1, y, width));
                }else
                    neighbors.add(-1);
                //set random connector for the bottom, if it is not the bottom line
                if(y != height - 1){
                    int bottomConnector = (int)(Math.random() * 2) + 1;
                    tempType |= bottomConnector << 4;
                    bottomConnector ^= 0b11;
                    setTypes[x] = bottomConnector;
                    neighbors.add(getIdByXY(x, y + 1, width));
                }else
                    neighbors.add(-1);
                if(x != 0)
                    neighbors.add(getIdByXY(x - 1, y, width));
                else
                    neighbors.add(-1);
                if(y != 0)
                    neighbors.add(getIdByXY(x, y - 1, width));
                else
                    neighbors.add(-1);
                puzzle.setConnectors(tempType);
                puzzle.setInGame(false);
                puzzle.setPuzzleId(id);
                puzzle.setOriginalId(id);
                puzzle.setComponentId(id);
                puzzle.setNeighbors(neighbors);
                puzzles.add(puzzle);
                id++;
            }
        }
        return puzzles;
    }

    public FieldParams setPuzzlesInStartField(List<Puzzle> puzzles, ImageCropParams params){
        log.info("Puzzles befire shuffle: {}", puzzles);
        Collections.shuffle(puzzles);
        log.info("Puzzles after shuffle: {}", puzzles);
        int puzzleOffset = (int)(params.puzzleSize() * 0.34247);
        int fieldWidth = params.puzzleSize() * (params.width() + 4) + (params.width() - 1) * puzzleOffset;
        int fieldHeight = params.puzzleSize() * (params.height() + 4) + (params.height() - 1) * puzzleOffset;
        int extPuzzleLength = params.puzzleSize() + 2 * puzzleOffset;
        int gridWidth = puzzlesGridWidth * extPuzzleLength;
        int offset = 0;
        if(gridWidth > fieldWidth){
            fieldWidth = gridWidth;
        }else{
            offset = (fieldWidth - gridWidth) / 2;
        }
        int gridHeight = (puzzles.size() / puzzlesGridWidth + 1) * extPuzzleLength;
        int i = 0;
        for(Puzzle p: puzzles){
            List<Integer> newNeighbors = new ArrayList<>();
            for(int neighborId: p.getNeighbors()){
                if(neighborId == -1){
                    newNeighbors.add(-1);
                }else{
                    int neighborIndex = 0;
                    for(Puzzle withId: puzzles){
                        if(withId.getOriginalId() == neighborId) break;
                        neighborIndex++;
                    }
                    newNeighbors.add(neighborIndex);
                }
            }
            p.setPuzzleId(i);
            p.setNeighbors(newNeighbors);
            int x = offset + (i % puzzlesGridWidth) * extPuzzleLength;
            int y = fieldHeight + i / puzzlesGridWidth * extPuzzleLength;
            p.setX((int)(10000. * x / fieldWidth));
            p.setY((int)(10000. / fieldWidth * y));
            i++;
        }
        log.info("Puzzles after ids update: {}", puzzles);
        return new FieldParams((int)(10000. / fieldWidth * fieldHeight), (int)(10000. / fieldWidth * gridHeight), (int)(10000. / fieldWidth * params.puzzleSize()));
    }
}
