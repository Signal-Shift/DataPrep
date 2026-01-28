package com.originspecs.specextractor;

import com.originspecs.specextractor.reader.FileReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {

        validateArgs(args);
        String inputPath = args[0];
        String outputPath = args[1];
        log.info("Processing file: {}", inputPath);

        FileReader reader = new FileReader();

        try{
            reader.readXls(inputPath, outputPath);
            log.info("Successfully transformed xls data to JSON file : {}", outputPath);
        } catch (Exception e){
            log.error("Critical error during file processing :", e);
        }


    }

    public static void validateArgs(String[] args) {
        if (args.length < 2){
            log.error("Usage: java -jar spec-extractor.jar <input-file> <output-file>");
            log.error("Example: java -jar spec-extractor inputFile.xls outputFile.json");
        }
    }
}
