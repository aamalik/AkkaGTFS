package api;

import integrations.GTFS2Neo4jImporter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class AdminApi {

	@Autowired
	SpatialDatabaseService spatialService;

	public String uploadGTFSFRorRegion() {

	    File newFile = new File("./tmp/Upload__HVV_Rohdaten_GTFS_Fpl_20170810.zip");
        String regionId = "55";
        boolean dryrun = true;

		File f;
		try {

			GTFS2Neo4jImporter importer = new GTFS2Neo4jImporter();
			System.out.println("spatialService: " + spatialService);
			importer.execute(spatialService, newFile, regionId, dryrun);
		} catch (IOException e) {
			log.error("Error while processing GTFS file", e);
		}
		return "redirect:/uploadStatus";
	}

    public static void main(String[] args) {
        AdminApi aa = new AdminApi();
        aa.uploadGTFSFRorRegion();
    }
}