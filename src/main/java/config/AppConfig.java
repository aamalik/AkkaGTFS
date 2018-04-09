package config;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.File;

@Slf4j
@Configuration
public class AppConfig {

	private GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File("./tmp/neotest"))
			.newGraphDatabase();

	private SpatialDatabaseService spatialService = new SpatialDatabaseService(db);

	@Bean
	SpatialDatabaseService getSpatialService() {
		return spatialService;
	};


	@PreDestroy
	public void destroy() {
		log.debug("Shutting down Neo4J");
		db.shutdown();
	}

}
