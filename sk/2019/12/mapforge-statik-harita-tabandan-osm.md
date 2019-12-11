# Mapsforge, OSM Bazlı Harita Tabanından Harita Görüntüsü Almak

Açık harita veri tabanı OSM bazlı bir diğer haritalama tabanı ve
kodlama altyapısı mapsforge. Eğer kendi diskimizdeki bir dosyadan
harita alıp basmak istiyorsak, en basit kod alttaki gibi

```
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

public class SaveTiles {

    private static final String SAVE_PATH = "/tmp/";

    private static final File DEFAULT_MAP_PATH = new File("[DIZIN]/kosovo.map");
    // Kosovoda herhangi bir nokta
    private static final double LAT = 42.470369;
    private static final double LNG = 20.838044;
    private static final byte ZOOM = 14;

    public static void main(String[] args) throws IOException {

        MapDataStore mapData = new MapFile(DEFAULT_MAP_PATH);

        final int ty = MercatorProjection.latitudeToTileY(LAT, ZOOM);
        final int tx = MercatorProjection.longitudeToTileX(LNG, ZOOM);
        Tile tile = new Tile(tx, ty, ZOOM, 800);

        GraphicFactory gf = AwtGraphicFactory.INSTANCE;
        XmlRenderTheme theme = InternalRenderTheme.OSMARENDER;
        DisplayModel dm = new FixedTileSizeDisplayModel(256);
        RenderThemeFuture rtf = new RenderThemeFuture(gf, theme, dm);
        RendererJob theJob = new RendererJob(tile, mapData, rtf, dm, 1.0f, false, false);
        File cacheDir = new File("/tmp", "tmp");
        FileSystemTileCache tileCache = new FileSystemTileCache(10, cacheDir, gf, false);
        TileBasedLabelStore tileBasedLabelStore = new TileBasedLabelStore(tileCache.getCapacityFirstLevel());

        DatabaseRenderer renderer = new DatabaseRenderer(mapData, gf, tileCache, tileBasedLabelStore, true, true, null);

        Thread t = new Thread(rtf);
        t.start();

        TileBitmap tb = renderer.executeJob(theJob);
        tileCache.put(theJob, tb);

        mapData.close();

        System.out.printf("Tile has been saved at %s/%d/%d/%d.tile.\n", cacheDir.getPath(), ZOOM, tx, ty);
    }
}
```

Bu kod için gereken jar dosyaları,

https://mvnrepository.com/artifact/net.sf.kxml/kxml2/2.3.0

https://mvnrepository.com/artifact/org.mapsforge/mapsforge-core/0.12.0

https://mvnrepository.com/artifact/org.mapsforge/mapsforge-core/0.12.0

https://mvnrepository.com/artifact/org.mapsforge/mapsforge-map-awt/0.12.0

https://mvnrepository.com/artifact/org.mapsforge/mapsforge-map-reader/0.12.0

https://mvnrepository.com/artifact/org.mapsforge/mapsforge-themes/0.12.0

https://mvnrepository.com/artifact/com.kitfox.svg/svg-salamander/1.0

Kodu derlemek icin gerekli iskelet dizi, Ant yapisi icin

[https://github.com/burakbayramli/kod/tree/master/sk/2019/12/staticmap](https://github.com/burakbayramli/kod/tree/master/sk/2019/12/staticmap)

Mapsforge tabanları pek çok ülke için bulunabilir, mesela Kosova için
(ufak olduğu için oraya gittik), `kosovo.map` indirilir,

[http://download.mapsforge.org/maps/v5/europe/](http://download.mapsforge.org/maps/v5/europe/)

istenilen dizine konup ona göre kod ayarlanınca, `ant run` ile
işletiriz, ve sonuç bir `tile` dosyası oluyor, alttaki gibi çıkacak,

![](kosovo.png)
![](https://1.bp.blogspot.com/-s95YnKzUq4o/Xe-qDizx-1I/AAAAAAAAB4g/OHJV4a_5A-cwtXkmUoBPDL8VN1PiGy86ACLcBGAsYHQ/s320/kosovo.png)


