package org.princehouse.mica.example;

import com.beust.jcommander.Parameter;
import org.princehouse.mica.base.ExternalSelectProtocol;
import org.princehouse.mica.base.model.Protocol;
import org.princehouse.mica.base.sugar.annotations.GossipUpdate;
import org.princehouse.mica.lib.abstractions.Overlay;

/**
 * Pull-based "find minimum value" example protocol
 *
 * @author lonnie
 */
public class FindMinPull extends ExternalSelectProtocol {

  private static final long serialVersionUID = 1L;

  @Parameter(names = "-x", description = "Initial value")
  private int x = 0;

  public FindMinPull(int x, Overlay overlay) {
    super(overlay);
    this.x = x;
  }

  @GossipUpdate
  @Override
  public void update(Protocol other) {
    FindMinPull o = (FindMinPull) other;
    int temp = Math.min(x, o.x);
    x = temp;
  }

}
