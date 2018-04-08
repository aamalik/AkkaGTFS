import java.io.File;
import java.io.IOException;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.SpatialRecord;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import com.vividsolutions.jts.geom.Coordinate;

public class Neo4jWorker {

    public String uploadGTFSForRegion() throws IOException {

        Neo4jWorker importer = new Neo4jWorker();
        importer.execute();

        return "File succesfully uploaded";
    }

    public void execute() throws IOException {


        GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File("./tmp/neotest")).newGraphDatabase();

        SpatialDatabaseService spatialService = new SpatialDatabaseService(database);

        final String LAYER_NAME = "stations";

        File gtfsFile = new File("tmp/Upload__HVV_Rohdaten_GTFS_Fpl_20170810.zip");

        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(gtfsFile);

        GtfsRelationalDaoImpl store = new GtfsRelationalDaoImpl();
        reader.setEntityStore(store);

        reader.run();

        // get or create a layer for station
        final EditableLayer layer = (spatialService.containsLayer(LAYER_NAME)
                ? (EditableLayer) spatialService.getLayer(LAYER_NAME)
                : (EditableLayer) spatialService.createSimplePointLayer(LAYER_NAME, "Longitude", "Latitude"));

        try (Transaction tx = spatialService.getDatabase().beginTx()) {

            store.getAllStops().forEach((s) -> {
                SpatialRecord record = layer.add(layer.getGeometryFactory().createPoint(new Coordinate(s.getLat(), s.getLon())));

                // now we add some further data to the spatial point
                Node n = record.getGeomNode();
                n.setProperty("id", s.getId().toString());
                n.setProperty("name", s.getName());

                System.out.println("Imported {}" +  s.getName());
                store.getStopTimesForStop(s).forEach((st) -> {
                    System.out.println("Found for this {}" + st.getRouteShortName());
                });

            });

            tx.success();

            System.out.println("Stored {} datasets to layer " + store.getAllStops().size() + " : " +  LAYER_NAME);
        }
        finally {
            System.out.println("Shutting database down");
            database.shutdown();
        }

    }


    public static void main(String[] args) throws IOException {

//        File f = new File("/tmp/neotest");
//        if(f.exists()){
//            System.out.println("Damn");
//        }
//        else{
//            System.out.println("wtf");
//        }
//        if(f.canWrite()){
//            System.out.println("great");
//        }

        Neo4jWorker neo4jWorker = new Neo4jWorker();
        neo4jWorker.uploadGTFSForRegion();

    }
}
