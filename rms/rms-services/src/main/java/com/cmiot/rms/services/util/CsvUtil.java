package com.cmiot.rms.services.util;


import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @Author xukai
 * Date 2016/6/17
 */
public class CsvUtil {

    // csv's default delemiter is ','
    private final static String DEFAULT_DELIMITER = ",";
    // Mark a new line
    private final static String DEFAULT_END = "\r\n";
    // If you do not want a UTF-8 ,just replace the byte array.
    private final static byte commonCsvHead[] = { (byte) 0xEF, (byte) 0xBB,
            (byte) 0xBF };

    /**
     * Write source to a csv file
     *
     * @param source
     * @throws IOException
     */
    public static void writeCsv(List<List<String>> source,String filePath) throws IOException {
        // Aoid java.lang.NullPointerException
        Preconditions.checkNotNull(source);
        StringBuilder sbBuilder = new StringBuilder();
        for (List<String> list : source) {
//            sbBuilder.append(Joiner.on(DEFAULT_DELIMITER).join(list)).append(
//                    DEFAULT_END);
            Iterator<String> parts = list.iterator();
            if (parts.hasNext()) {
                sbBuilder.append(parts.next());
                while (parts.hasNext()) {
                    sbBuilder.append(DEFAULT_DELIMITER);
                    sbBuilder.append(parts.next());
                }
            }
            sbBuilder.append(DEFAULT_END);
        }
        Files.write(Bytes.concat(commonCsvHead,
                sbBuilder.toString().getBytes(Charsets.UTF_8.toString())),
                new File(filePath));
    }

    /**
     * Simple read a csv file
     *
     * @param file
     * @throws IOException
     */
    public static void readCsv(File file) throws IOException {
        System.out.println(Files.readFirstLine(file, Charsets.UTF_8));
    }

}
