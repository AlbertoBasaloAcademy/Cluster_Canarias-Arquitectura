package com.astrobookings;

import com.astrobookings.fleet.infrastructure.FleetFactory;
import com.astrobookings.sales.infrastructure.SalesFactory;

/**
 * Composition Root for the AstroBookings application.
 * Assembles the two main modules: Fleet and Sales.
 * 
 * Fleet module is created first (Supporting Subdomain).
 * Sales module is created second and depends on Fleet (Core Domain).
 */
public class Config {
  // Module factories
  static final FleetFactory fleetFactory = new FleetFactory();
  static final SalesFactory salesFactory = new SalesFactory(fleetFactory);
}
