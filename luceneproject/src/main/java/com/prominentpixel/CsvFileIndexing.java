package com.prominentpixel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        Directory fsDirectory= FSDirectory.open(Paths.get(properties.getProperty("INDEX_PATH")));

        IndexWriterConfig config=new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer=new IndexWriter(fsDirectory,config);
        File file=new File(properties.getProperty("CSV_PATH"));

        File[] list=file.listFiles();
        for (File fileName:list)
        {
             String filename=fileName.getAbsolutePath();
             List<String> fileLines= Files.readAllLines(Paths.get(filename));
             SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy");
            //Take Header String here it will always be at the 0th Index.
            String headerLine = fileLines.get(0);
            String[] headers = headerLine.split(",");
            String value=headers[0];

            fileLines.remove(0);

            for (String line:fileLines)
            {
                String[] data=line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);
                try {
                    Document document=new Document();
                    int dataIndex =0;
                   String date=data[dataIndex];
                   Date dates=dateFormat.parse(date);
                    for(String header : headers)
                    {
                        if(header==value)
                        {
                            try {
                                document.add(new StringField(header, String.valueOf(dates), Field.Store.YES));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                            dataIndex++;
                        }
                        else
                            {
                                document.add(new TextField(header, data[dataIndex++], Field.Store.YES));
                            }
                    }

                    writer.addDocument(document);

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        writer.close();

        System.out.println("Enter a column name=");
        String column=scanner.next();
        System.out.println("enter search value=");
        String searchValue=scanner.next();
        Term term=new Term(column,searchValue);
        Query query=new TermQuery(term);

        IndexReader indexReader = DirectoryReader.open(fsDirectory);
        IndexSearcher searcher=new IndexSearcher(indexReader);
        TopDocs topDocs=searcher.search(query,100);
        ScoreDoc[] scoreDocs=topDocs.scoreDocs;
        for (int i=0;i<scoreDocs.length;i++)
        {
            int docId=scoreDocs[i].doc;

            Document documents=searcher.doc(docId);
            System.out.println(documents.get(column));
        }
        indexReader.close();

        scanner.close();

    }
}
