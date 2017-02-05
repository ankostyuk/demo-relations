package ru.nullpointer.nkbrelation.dao;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import ru.nullpointer.nkbrelation.data.ImportBsnRelations;

/**
 *
 * @author Alexander Yastrebov
 */
public class TestGraphDatabaseFactory implements FactoryBean<GraphDatabaseService>, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(TestGraphDatabaseFactory.class);
    //
    private String bsnRelations;
    //
    private File dbPath;
    private GraphDatabaseService db;

    public void setBsnRelations(String bsnRelations) {
        this.bsnRelations = bsnRelations;
    }

    @Override
    public GraphDatabaseService getObject() throws Exception {
        dbPath = createTempDir();

        logger.debug("create: {}, bsn relations: {}", dbPath, bsnRelations);

        if (bsnRelations != null) {
            ImportBsnRelations.main(new String[]{
                new File(bsnRelations).getAbsolutePath(),
                dbPath.getAbsolutePath()
            });
        }
        db = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        return db;
    }

    @Override
    public Class<?> getObjectType() {
        return EmbeddedGraphDatabase.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        logger.debug("destroy: {}", dbPath);

        db.shutdown();
        deleteTempDir(dbPath);
    }

    private static File createTempDir() throws IOException {
        File db;
        db = File.createTempFile("neo4j", Long.toString(System.nanoTime()));
        db.delete();
        db.mkdir();
        return db;
    }

    private static void deleteTempDir(File tempDir) throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }
}
