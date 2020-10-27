package com.prominentpixel;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class CsvFileIndexing {
    public static void main(String[] args) throws IOException, ParseException {
        Scanner scanner=new Scanner(System.in);
        System.out.print("Enter a Properties File Path=");
        String propertiesFilesPath=scanner.next();
        FileReader reader=new FileReader(propertiesFilesPath);
        Properties properties=new Properties();
        properties.load(reader);

        System.out.print("Properties File Index Path key=");
        String indexPath=scanner.next();
        Directory fsDirectory= FSDirectory.open(Paths.get(properties.getProperty(indexPath)));

        IndexWriterConfig config=new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer=new IndexWriter(fsDirectory,config);
        System.out.print("Properties Csv File Path Key=");
        String csvFilePath=scanner.next();
        List<String> fileLines= Files.readAllLines(Paths.get(properties.getProperty(csvFilePath)));


        SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MMM-yy");
        //Take Header String here it will always be at the 0th Index.
        String headerLine = fileLines.get(0);
        String[] headers = headerLine.split(",");
        for (String line:fileLines)
        {
            String[] data=line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);
            try {
                Document document=new Document();
                int dataIndex = 0;
                for(String header : headers){
                    document.add(new StringField(header, data[dataIndex++], Field.Store.YES));
                }
                writer.addDocument(document);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        scanner.close();
        writer.commit();
        writer.close();
    }
}
