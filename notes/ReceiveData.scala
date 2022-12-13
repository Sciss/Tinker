val cfg = osc.UDP.Config()
cfg.localPort = 7771
val rcv = osc.UDP.Receiver(cfg)
rcv.connect()
rcv.dump()
rcv.dump(osc.Dump.Off)
rcv.close()
