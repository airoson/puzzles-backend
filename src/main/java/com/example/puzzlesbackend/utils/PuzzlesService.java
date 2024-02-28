package com.example.puzzlesbackend.utils;

import com.example.puzzlesbackend.entities.Puzzle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class PuzzlesService {

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
                puzzle.setPuzzleId(id);
                puzzle.setInGame(false);
                puzzle.setComponentId(id);
                puzzle.setNeighbors(neighbors);
                puzzles.add(puzzle);
                id++;
            }
        }
        return puzzles;
    }
}
