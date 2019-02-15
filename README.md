# S2S

S2S
Simulation to Simulation

S2S is a piece of light-weight software infrastructure that acts as a harness & controller for integrating simulators and simulations.
S2S is designed to act as a connection broker between simulations and to manage their operation and interaction. It does not itself interact directly with the contents of streams of information produced or consumed by simulations.

Technical features of the harness are:

•	Exchange of objects using TCP/IP over streams

•	Google Protocol Buffers (GBP) as a language neutral mechanism for simulation event streams

•	GBP for harness/simulation control (by default, a single control bus)

•	Minimal static configuration

•	Plug & play using network discovery

•	Avoids central bottleneck

•	Customisation and extensibility achieved through simulator stubs not by extensions to S2S itself

•	S2S is written in Java

•	Use of socket streams (by default) for both harness control and event streams


Mediating and translating the output from and input to simulations can be added by configuring new GBP structures. In this way, integrating features for inter-simulation communication is achieved by a mix of defining GBP structures and writing bespoke code in the form of simulation stubs that produce and consume streams from other simulations in the harness cluster.

All the management and communication data structures that are shared by stubs are defined in GBP form at a handshake level. This makes negotiation, versioning both efficient, facilitates upgrades and extensibility.

S2S will support:
-	Event filtering and prioritisation - to optimise the bandwidth used by the data streams and the order in which events are placed on streams
-	Mechanisms to support both synchronous and asynchronous simulation
-	Clock synchronisation
-	Co-ordinate negotiation (mediate matching/translation of co-ordinate systems)

 
WIP: Sketch of Harness and Stub Functionality
Harness
Acceptance of cluster member registration
This entails listening for new simulators registering with the harness.
Queries
Answering queries arriving on the harness’s control stream. These are likely to be about configuration information (versions of GBP messages to allow for resilience to change i.e. CM), information about the cluster of simulators connected by the harness and their states (IP addresses, network bandwidths) or statistics on the traffic volumes flowing in the cluster (depends on instrumentation). This functionality will also be of use for harness administration and monitoring as a CLI might be envisaged that sends GBP messages over the Kafka control stream and listens out for corresponding admin messages coming back.
Management functions
Start, heartbeat, stop
Time Sync
Follow a time sync protocol that stubs participate in.
Co-ordinate Sync
Instrumentation & Debug Functions
Version checking and management
Stubs
Discover and register with harness (including registration of GBP messages supported)
Queries to harness e.g. get harness cluster member list and set of GBPs that they use 
Filter settings e.g. positive and negative
Version checking and management
