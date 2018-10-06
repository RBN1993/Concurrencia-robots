// v1.0.1

import es.upm.babel.cclib.ConcIO;

public class LogisticaMonitor {
	public static final void main(final String[] args) throws InterruptedException {
		ControlAccesoNaves can = new ControlAccesoNavesMonitor();    
		Thread[] robot = new Thread[Robots.N_ROBOTS];
		for (int rid = 0; rid < Robots.N_ROBOTS; rid++) {
			robot[rid] = new ControlRobot(rid,can);
		}
		for (Thread t : robot) {
			t.start();
		}
		for (Thread t : robot) {
			t.join();
		}
	}
}

class ControlRobot extends Thread {
	private int rid;
	private ControlAccesoNaves can;

	protected ControlRobot () {
	}

	public ControlRobot (int rid, ControlAccesoNaves can) {
		this.rid = rid;
		this.can = can;
	}
	public static int numRobots;


	public void run () {
		int pesoAnterior;
		int peso = Robots.PESO_VACIO;
		for (int nid = 0; nid < Robots.N_NAVES; nid++) {
			ConcIO.printfnl("Robot " + rid + " solicitando entrar en nave " + nid + " con peso " + peso);
			can.solicitarEntrar(nid,peso);
			ConcIO.printfnl("Robot " + rid + " entrando en nave " + nid);
			Robots.entrarEnNave(rid,nid);
			ConcIO.printfnl("Robot " + rid + " en nave " + nid);
			ConcIO.printfnl("Robot " + rid + " cargando");
			pesoAnterior = peso;
			peso = Robots.cargar(rid,nid);
			ConcIO.printfnl("Robot " + rid + " pesa " + peso);
			ConcIO.printfnl("Robot " + rid + " solicitando salir de nave " + nid);
			can.solicitarSalir(nid,pesoAnterior);
			ConcIO.printfnl("Robot " + rid + " saliendo de nave " + nid);
			Robots.entrarEnPasillo(rid,nid+1);
			if (nid < Robots.N_NAVES - 1) {
				ConcIO.printfnl("Robot " + rid + " en pasillo de nave " + (nid+1));
			}
		}
		ConcIO.printfnl("Robot " + rid + " termina el recorrido con peso " + peso);
		ConcIO.printfnl("Han terminado :"+ ++numRobots);
	}
}
