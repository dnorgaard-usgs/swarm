package gov.usgs.volcanoes.swarm.map;

import gov.usgs.volcanoes.core.math.proj.GeoRange;
import gov.usgs.volcanoes.core.math.proj.Projection;
import gov.usgs.volcanoes.swarm.Metadata;
import gov.usgs.volcanoes.swarm.SwarmConfig;
import gov.usgs.volcanoes.swarm.event.PickData;
import gov.usgs.volcanoes.swarm.wave.WaveClipboardFrame;
import gov.usgs.volcanoes.swarm.wave.WaveViewPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

/**
 * This layer will draw S-P circles on the map.
 * @author Diana Norgaard
 *
 */
public class SpLayer implements MapLayer {

  private static final float[] dash = { 2.0f };
  private static final BasicStroke dashed =
      new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
  private static final BasicStroke solid =
      new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

  /**
   * Constructor.
   */
  public SpLayer() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Update list of S-P circles to draw.
   */
  private synchronized Vector<Sp> updateSpList() {
    Vector<Sp> spList = new Vector<Sp>();
    List<WaveViewPanel> waves = WaveClipboardFrame.getInstance().getWaves();
    if (WaveClipboardFrame.getInstance().isPickEnabled()) {
      for (WaveViewPanel wvp : waves) {
        PickData pickData = wvp.getPickData();
        if (pickData != null && pickData.isPlot() 
            && pickData.getPick(PickData.P) != null
            && pickData.isPickChannel(PickData.P)) {
          String channel = wvp.getChannel();
          double distance = pickData.getSpDistance();
          if (!Double.isNaN(distance)) {
            spList.add(new Sp(channel, distance, pickData.getSpMinDistance(),
                pickData.getSpMaxDistance()));
          }
        }
      }
    }
    return spList;
  }
  
  /**
   * @see gov.usgs.volcanoes.swarm.map.MapLayer#draw(java.awt.Graphics2D)
   */
  public void draw(Graphics2D g2) {
    Color color = g2.getColor();
    Stroke stroke = g2.getStroke();
    Shape clip = g2.getClip();
    g2.setColor(new Color(SwarmConfig.getInstance().mapLineColor));
    
    // First get updated list of S-P
    Vector<Sp> spList = updateSpList();
    if (spList.size() == 0) {
      return;
    }

    MapPanel  panel = MapFrame.getInstance().getMapPanel();
    
    // set clip
    int widthPx = panel.getGraphWidth();
    int heightPx = panel.getGraphHeight();
    int inset = panel.getInset();
    Rectangle2D rectangle = new Rectangle2D.Double(inset, inset, widthPx, heightPx);
    g2.setClip(rectangle);
    
    // draw
    GeoRange range = panel.getRange();
    Projection projection = panel.getProjection();
    final double[] ext = range.getProjectedExtents(projection);
    final double dx = (ext[1] - ext[0]);
    for (Sp sp : spList) {
      Metadata md = SwarmConfig.getInstance().getMetadata(sp.channel);
      if (md == null) {
        continue;
      }
      if (md.hasLonLat()) {
        Point2D.Double center = panel.getXy(md.getLongitude(), md.getLatitude());

        // draw S-P circle
        g2.setStroke(solid);
        double radius = widthPx * 1000 * sp.distance / dx;
        g2.drawOval((int) (center.x - radius), (int) (center.y - radius), (int) radius * 2,
            (int) radius * 2);
        // draw S-P uncertainty
        g2.setStroke(dashed);
        if (!Double.isNaN(sp.minDistance)) {
          double minRadius = widthPx * 1000 * sp.minDistance / dx;
          g2.drawOval((int) (center.x - minRadius), (int) (center.y - minRadius),
              (int) minRadius * 2, (int) minRadius * 2);
        }
        if (!Double.isNaN(sp.maxDistance)) {
          double maxRadius = widthPx * 1000 * sp.maxDistance / dx;
          g2.drawOval((int) (center.x - maxRadius), (int) (center.y - maxRadius),
              (int) maxRadius * 2, (int) maxRadius * 2);
        }
      }
    }
    
    g2.setColor(color);
    g2.setStroke(stroke);
    g2.setClip(clip);
  }
  
  /**
   * S-P data.
   */
  class Sp {
    private String channel;
    private double distance;
    private double minDistance;
    private double maxDistance;

    Sp(String channel, double distance, double minDistance, double maxDistance) {
      this.channel = channel;
      this.distance = distance;
      this.minDistance = minDistance;
      this.maxDistance = maxDistance;
    }
  }

  public boolean mouseClicked(MouseEvent e) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setVisible(boolean isVisible) {
    
  }

  public boolean mouseMoved(MouseEvent e) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setMapPanel(MapPanel mapPanel) {
    // TODO Auto-generated method stub
    
  }

}
