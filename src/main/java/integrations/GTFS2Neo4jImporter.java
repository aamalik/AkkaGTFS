package integrations;

import com.vividsolutions.jts.geom.Coordinate;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.SpatialRecord;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;


@Slf4j
public class GTFS2Neo4jImporter {

	final static String LAYER_NAME = "stations";

	public void execute(SpatialDatabaseService spatialService, String pathname, String regionId) throws IOException {
		execute(spatialService, new File(pathname), regionId);
	}

	public void execute(SpatialDatabaseService spatialService, File gtfsFile, String regionId) throws IOException {
		execute(spatialService, gtfsFile, regionId, false);
	}

	public void execute(SpatialDatabaseService spatialService, File gtfsFile, String regionId, boolean dryrun)
			throws IOException {

        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File("./tmp/neotest"))
                .newGraphDatabase();

        GtfsReader reader = new GtfsReader();
		reader.setInputLocation(gtfsFile);

		GtfsRelationalDaoImpl store = new GtfsRelationalDaoImpl();
		reader.setEntityStore(store);

		reader.run();
        try {

            spatialService = new SpatialDatabaseService(db);

            // get or create a layer for station
            final EditableLayer layer = (spatialService.containsLayer(LAYER_NAME)
                    ? (EditableLayer) spatialService.getLayer(LAYER_NAME)
                    : (EditableLayer) spatialService.createSimplePointLayer(LAYER_NAME, "Longitude", "Latitude"));

            try (Transaction tx = spatialService.getDatabase().beginTx()) {
                store.getAllStops().forEach((s) -> {
                    if (!dryrun) {
                        SpatialRecord record = layer
                                .add(layer.getGeometryFactory().createPoint(new Coordinate(s.getLat(), s.getLon())));
                        // now we add some further data to the spatial point
                        Node n = record.getGeomNode();
                        n.setProperty("id", s.getId().toString());
                        n.setProperty("name", s.getName());
                        n.setProperty("regionId", regionId);
                    }
                    log.info("Imported {}", s.getName());
                    store.getStopTimesForStop(s).forEach((st) -> {
                        log.info("Found for this {}", st.getRouteShortName());
                    });

                });
                tx.success();

                log.info("Stored {} datasets to layer ", store.getAllStops().size(), LAYER_NAME);
            }
        } finally {
            db.shutdown();
        }
	}
}
