import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.labels.TileBasedLabelStore;
import org.mapsforge.map.layer.renderer.DatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.FixedTileSizeDisplayModel;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;
import java.io.File;
import java.io.IOException;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;

public class SaveTiles {

    private static File DEFAULT_MAP_PATH;
    private static byte ZOOM;

    public static void main(String[] args) throws IOException {	
	
	DEFAULT_MAP_PATH = new File(args[2]);

	ZOOM = (byte)Integer.parseInt(args[3]);
	
	String [] tokens = args[0].split(",");	
	    	
	double LAT = Double.parseDouble(tokens[0].split(";")[0]);
	double LNG = Double.parseDouble(tokens[0].split(";")[1]);
        // TODO Use args for all parameters

        // Load map.
        MapDataStore mapData = new MapFile(DEFAULT_MAP_PATH);

        // Assign tile.
        final int ty = MercatorProjection.latitudeToTileY(LAT, ZOOM);
        final int tx = MercatorProjection.longitudeToTileX(LNG, ZOOM);
        Tile tile = new Tile(tx, ty, ZOOM, 800);
	
	System.out.print("{\"pixels\":[");
	//System.out.println("abs="+MercatorProjection.getPixelRelativeToTile(new LatLong(40.968254,29.080640), tile));
	for (int i=0;i<tokens.length;i++){
	    //System.out.println(tokens[i]);
	    double currlat = Double.parseDouble(tokens[i].split(";")[0]);
	    double currlng = Double.parseDouble(tokens[i].split(";")[1]);
	    //System.out.println("abs="+MercatorProjection.getPixelRelativeToTile(new LatLong(currlat,currlng), tile));
	    Point pix = MercatorProjection.getPixelRelativeToTile(new LatLong(currlat,currlng), tile);
	    System.out.print("[" + pix.x + ","+pix.y+"]");
	    if (i<tokens.length-1) {
		System.out.print(",");
	    }	    
	}
	System.out.print("]");

        // Create requirements.
        GraphicFactory gf = AwtGraphicFactory.INSTANCE;
        XmlRenderTheme theme = InternalRenderTheme.OSMARENDER;
        DisplayModel dm = new FixedTileSizeDisplayModel(256);
        RenderThemeFuture rtf = new RenderThemeFuture(gf, theme, dm);
        RendererJob theJob = new RendererJob(tile, mapData, rtf, dm, 1.0f, false, false);
        File cacheDir = new File(args[1], "");
        FileSystemTileCache tileCache = new FileSystemTileCache(10, cacheDir, gf, false);
        TileBasedLabelStore tileBasedLabelStore = new TileBasedLabelStore(tileCache.getCapacityFirstLevel());

        // Create renderer.
        DatabaseRenderer renderer = new DatabaseRenderer(mapData, gf, tileCache, tileBasedLabelStore, true, true, null);

        // Create RendererTheme.
        Thread t = new Thread(rtf);
        t.start();

        // Draw tile and save as PNG.
        TileBitmap tb = renderer.executeJob(theJob);
        tileCache.put(theJob, tb);

        // Close map.
        mapData.close();

        System.out.printf(",\"file\": \"%s/%d/%d/%d.tile\"", cacheDir.getPath(), ZOOM, tx, ty);
	System.out.print("}");
	
    }
}