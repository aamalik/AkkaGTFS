import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class jsonTest {

    public ArrayList<LatLon> extractLatLonFromFile() throws XMLStreamException, IOException {

        final AsyncXMLInputFactory f = new InputFactoryImpl();
        AsyncXMLStreamReader<AsyncByteArrayFeeder> sr = null;

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("abfahrtCar.gpx").getFile());

        //init array with file length
        byte[] xmlBytes = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        fis.read(xmlBytes); //read file into bytes[]
        fis.close();

        sr = f.createAsyncFor(xmlBytes);
        ArrayList<LatLon> latlonList = new ArrayList<LatLon>();

        int bufferFeedLength = 1 ;
        int currentByteOffset = 0 ;
        int type ;
        do{
            while( (type = sr.next() ) == AsyncXMLStreamReader.EVENT_INCOMPLETE ) {
                break;
//                byte[] buffer = new byte[]{ xmlBytes[ currentByteOffset ] } ;
//                currentByteOffset ++ ;
//                System.out.println("buffer: " + buffer);
//                sr.getInputFeeder().feedInput( buffer, 0, bufferFeedLength ) ;
//                if( currentByteOffset >= xmlBytes.length ) {
//                    sr.getInputFeeder().endOfInput() ;
//                }
            }
            switch( type ) {

                case XMLEvent.START_DOCUMENT :
//                    System.out.println( "start document" ) ;
                    break ;

                case XMLEvent.START_ELEMENT :
//                    System.out.println( "start element: " + sr.getName());

                    if(sr.getName().toString().contains("trkpt")){

                        latlonList.add(new LatLon(Double.parseDouble(sr.getAttributeValue(0)),
                                Double.parseDouble(sr.getAttributeValue(1))));
                        System.out.println(sr.getAttributeName(0) + " " + sr.getAttributeValue(0));
                        System.out.println(sr.getAttributeName(1) + " " + sr.getAttributeValue(1));
                    }
                    break ;

                case XMLEvent.CHARACTERS :
//                    System.out.println( "characters: " + sr.getText()) ;
                    break ;

                case XMLEvent.END_ELEMENT :
//                    System.out.println( "end element: " + sr.getName()) ;
                    break ;

                case XMLEvent.END_DOCUMENT :
//                    System.out.println( "end document" ) ;
                    break ;

                default :
                    break ;
            }
            sr.getInputFeeder().endOfInput() ;
        } while( type != XMLEvent.END_DOCUMENT ) ;
        sr.close();

        System.out.println("FINAL LIST: " + latlonList.toString());

        return latlonList;

    }

    public static void main(String[] args) throws IOException, XMLStreamException {

        jsonTest jt = new jsonTest();
        ArrayList<LatLon> output = jt.extractLatLonFromFile();

        System.out.println("output: " + output.size());


    }
}
