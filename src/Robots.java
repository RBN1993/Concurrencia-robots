// v1.0

import es.upm.babel.cclib.Monitor;

class Robots {
	public static final int N_ROBOTS = 50;
	public static final int N_NAVES = 4;
	public static final int MAX_PESO_EN_NAVE = 1000;
	public static final int PESO_VACIO = 100;

	private static Monitor mutex = new Monitor();

	/* nave[rid] = nid <=> el robot rid está en la nave nid, nid == -1
	 * está entrando u ocupando un pasillo, nid == N_NAVES está fuera */
	private static int nave[] = new int[N_ROBOTS];
	static {
		for (int rid = 0; rid < N_ROBOTS; rid++) {
			nave[rid] = -1;//La nave tiene tantos robots pero hay uno en el pasillo o esta entrando
		}
	}

	/* peso[rid] es el peso real del robot */
	private static int peso[] = new int[N_ROBOTS];
	static {
		for (int rid = 0; rid < N_ROBOTS; rid++) {
			peso[rid] = PESO_VACIO;
		}
	}

	/* pesoEntrada[rid] es el peso con el que el robot entró en la nave */
	private static int pesoEntrada[] = new int[N_ROBOTS];

	/* ocupa[nid] == rid <=> el pasillo de entrada de la nave nid está
	 * ocupado por el robot rid, -1 si no está ocupado */
	private static int ocupa[] = new int[N_NAVES];
	static {
		for (int nid = 0; nid < N_NAVES; nid++) {
			ocupa[nid] = -1;
		}
	}

	/* Calcula el peso con el que entraron los robots en la nave */
	private static int pesoNave(int nid) {
		int p = 0;
		for (int rid = 0; rid < N_ROBOTS; rid ++) {
			p += nave[rid] == nid ? pesoEntrada[rid] : 0;
		}
		return p;
	}

	/* El robot rid entra en el pasillo de la nave nid sin comprobar si
	 * se encuentra ocupado. Si el robot no tiene acceso a dicho pasillo
	 * (ej. no se encuentra en la nave nid - 1) o el pasillo no existe
	 * (ej. nid == 0) entonces la orden es ignorada y retorna
	 * inmediatamente.  Al invocarlo con nid == N_NAVES el robot
	 * símplemente sale de la última nave. Esta orden termina de
	 * ejecutarse cuando el robot ha terminado de entrar en el
	 * pasillo. */
	public static void entrarEnPasillo(int rid, int nid) {
		if (nave[rid] == nid - 1) {
			// Lo primero es desocupar la nave (así la contabilidad del peso es apropiada)
			nave[rid] = -1;
			sleep(100);
			mutex.enter();
			if (nid < N_NAVES && ocupa[nid] >= 0) {
				throw new RuntimeException("El robot " + rid + " intenta entrar en pasillo " + nid + " ocupado por  " + ocupa[nid]);
			}
			// Ocupa el pasillo (excepto si nid == N_NAVES)
			if (nid < N_NAVES) {
				ocupa[nid] = rid;
			}
			mutex.leave();
		}
	}

	/* El robot rid entra en la nave nid sin comprobar sobrepeso. Si el
	 * robot no estaba en el pasillo de entrada de la nave nid entonces
	 * la orden es ignorada y retorna inmediatamente. Esta orden termina
	 * de ejecutarse cuando el robot ha accedido a la nave nid. */
	public static void entrarEnNave(int rid, int nid) {
		if (nid == 0 && nave[rid] == -1
				|| ocupa[nid] == rid) {
			// Lo primero es liberar el pasillo
			ocupa[nid] = -1;
			sleep(100);
			mutex.enter();
			int pesoNave = pesoNave(nid);
			int pesoRobot = peso[rid];
			if (pesoNave + pesoRobot > MAX_PESO_EN_NAVE) {
				throw new RuntimeException("El robot " + rid + " con peso " + pesoRobot + " intenta entrar en nave " + nid + " que soporta peso " + pesoNave);
			}
			// Entra en la nave
			nave[rid] = nid;
			// Recuerda el peso a la entrada
			pesoEntrada[rid] = pesoRobot;
			mutex.leave();
		}
	}

	/* El robot rid realiza la carga programada en la nave nid y
	 * devuelve el peso total del robot (que siempre es inferior a
	 * MAX_PESO_EN_NAVE). Si el robot no estaba en la nave nid entonces
	 * la orden es ign.orada y retorna inmediatamente.  Esta orden
	 * termina de ejecutarse cuando el robot tiene toda la carga y se
	 * posiciona en una zona segura (el incremento de peso no supone
	 * problema para el suelo de la nave). */
	public static int cargar(int rid, int nid) {
		if (nave[rid] == nid) {
			mutex.enter();
			int carga = (int)(Math.random() * (MAX_PESO_EN_NAVE - pesoNave(nid)));
			peso[rid] += carga;
			mutex.leave();
			sleep(carga);
		}
		return peso[rid];
	}

	// A sleep with no exceptions
	private static void sleep (int ms) {
		long initialTimeMillis = System.currentTimeMillis();
		long remainTimeMillis = ms;

		while (remainTimeMillis > 0) {
			try {
				Thread.sleep(remainTimeMillis);
			}
			catch (InterruptedException e) {
			}
			remainTimeMillis =
					ms - (System.currentTimeMillis() - initialTimeMillis);
		}
	}
}
