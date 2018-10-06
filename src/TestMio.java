
public class TestMio {
	public static void main(String[] args) {
		final ControlAccesoNavesMonitor m = new ControlAccesoNavesMonitor();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				  m.solicitarEntrar(0,1000); //del robot 0 -- did not block
				  try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  m.solicitarSalir(0,1000);
			}
		});
		Thread t2 = new Thread(new Runnable() {	
			@Override
			public void run() {try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				  m.solicitarEntrar(0,500); //del robot 1 -- blocked
			}
		});
		Thread t3 = new Thread(new Runnable() {	
			@Override
			public void run() {try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				  m.solicitarEntrar(0,300); //del robot 2 -- blocked
			}
		});
		t.start();
		t2.start();
		t3.start();
	}
}
