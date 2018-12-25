import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

public class Sample5_2 extends Applet implements Runnable {

	private static final int MAZE_WIDTH = 57; // 幅：奇数
	private static final int MAZE_HEIGHT = 27; // 高：奇数
	private static final int BLOCK_WIDTH = 3;
	private static final int BLOCK_HEIGHT = 3;
	Thread thread = null;
	Dimension size;
	Image back;
	Graphics buffer;

	int block[][];

	//          上　右　下　左
	int dx[] = { 0, 1, 0, -1 };
	int dy[] = { -1, 0, 1, 0 };

	int stage;
	int marux; /* 迷路を解く人 */
	int maruy;
	int marud; /* 向き 0～3 */
	int oldx; /* 1つ前の位置 */
	int oldy;

	public void init() {
		resize(MAZE_WIDTH * BLOCK_WIDTH,
				MAZE_HEIGHT * BLOCK_HEIGHT);

		stage = 0;

		marux = 1;
		maruy = 1;
		marud = 0;
		oldx = marux;
		oldy = maruy;

		size = getSize();
		back = createImage(size.width, size.height);
		buffer = back.getGraphics();

		block = new int[MAZE_HEIGHT][MAZE_WIDTH];
		makeMaze();

		thread = new Thread(this);
		thread.start();
	}

	private void makeMaze() {
		/* 全体をクリア */
		for (int y = 0; y < MAZE_HEIGHT; y++) {
			for (int x = 0; x < MAZE_WIDTH; x++) {
				block[y][x] = 0;
			}
		}

		/* 外枠をセット */
		for (int x = 0; x < MAZE_WIDTH; x++) {
			block[0][x] = 1;
			block[MAZE_HEIGHT - 1][x] = 1;
		}
		for (int y = 0; y < MAZE_HEIGHT; y++) {
			block[y][0] = 1;
			block[y][MAZE_WIDTH - 1] = 1;
		}

		/* 基準点をセット */
		int px = (MAZE_WIDTH - 1) / 2 - 1;
		int py = (MAZE_HEIGHT - 1) / 2 - 1;
		for (int y = 1; y <= py; y++) {
			for (int x = 1; x <= px; x++) {
				block[y * 2][x * 2] = 1;
			}
		}

		/* 迷路作成 */
		for (int y = 1; y <= py; y++) {
			for (int x = 1; x <= px; x++) {
				if (y == 1) {
					int d = (int) (Math.random() * 4); // 上下左右
					block[y * 2 + dy[d]][x * 2 + dx[d]] = 1;
				} else {
					boolean flag = true;
					while (flag) {
						int d = (int) (Math.random() * 3) + 1; // 左右下
						if (block[y * 2 + dy[d]][x * 2 + dx[d]] == 0) {
							block[y * 2 + dy[d]][x * 2 + dx[d]] = 1;
							flag = false;
						}
					}
				}
			}
		}
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		if (stage == 0 || stage == 3) {
			buffer.setColor(Color.GRAY);
			buffer.fillRect(0, 0, size.width, size.height);

			for (int y = 0; y < MAZE_HEIGHT; y++) {
				for (int x = 0; x < MAZE_WIDTH; x++) {
					if (block[y][x] == 1) {
						buffer.setColor(Color.LIGHT_GRAY);
						buffer.fillRect(
								x * BLOCK_WIDTH, y * BLOCK_HEIGHT,
								BLOCK_WIDTH, BLOCK_HEIGHT);

						buffer.setColor(Color.DARK_GRAY);
						buffer.drawRect(
								x * BLOCK_WIDTH + 1, y * BLOCK_HEIGHT + 1,
								BLOCK_WIDTH - 2, BLOCK_HEIGHT - 2);
					} else if (block[y][x] == 3) {
						buffer.setColor(Color.red);
						buffer.fillRect(
								x * BLOCK_WIDTH, y * BLOCK_HEIGHT,
								BLOCK_WIDTH, BLOCK_HEIGHT);
					} else if (block[y][x] == 2 || block[y][x] == 4) {
						buffer.setColor(Color.yellow);
						buffer.fillRect(
								x * BLOCK_WIDTH, y * BLOCK_HEIGHT,
								BLOCK_WIDTH, BLOCK_HEIGHT);
					}
				}
			}

			buffer.setColor(Color.green);
			buffer.fillRect(
					BLOCK_WIDTH + 1, BLOCK_HEIGHT + 1,
					BLOCK_WIDTH - 2, BLOCK_HEIGHT - 2);

			buffer.setColor(Color.darkGray);
			buffer.fillRect(
					(MAZE_WIDTH - 2) * BLOCK_WIDTH + 1,
					(MAZE_HEIGHT - 2) * BLOCK_HEIGHT + 1,
					BLOCK_WIDTH - 2,
					BLOCK_HEIGHT - 2);
		} else if (stage == 1 || stage == 2) {
			buffer.setColor(Color.blue);
			buffer.fillRect(
					oldx * BLOCK_WIDTH + 1,
					oldy * BLOCK_HEIGHT + 1,
					BLOCK_WIDTH - 2,
					BLOCK_HEIGHT - 2);

			buffer.setColor(Color.green);
			buffer.fillRect(
					marux * BLOCK_WIDTH + 1,
					maruy * BLOCK_HEIGHT + 1,
					BLOCK_WIDTH - 2,
					BLOCK_HEIGHT - 2);
		}
		g.drawImage(back, 0, 0, this);
	}

	public void run() {
		while (true) {
			if (stage == 0) {
				makeMaze(); /* 迷路作成 */

				repaint();

				/* 500ミリ秒待機する */
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				stage = 1;

				marux = 1; /* スタート地点を初期化 */
				maruy = 1;
				marud = 0;
				oldx = marux;
				oldy = maruy;

			} else if (stage == 1) {
				/* 迷路を解いている途中 */
				repaint();

				/* 30ミリ秒待機する */
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
				}

				move();

				if ((marux == MAZE_WIDTH - 2) && (maruy == MAZE_HEIGHT - 2)) {
					/* ゴールに着いたら地図初期化へ */
					stage = 2;

					marux = 1; /* スタート地点を初期化 */
					maruy = 1;
					marud = 0;
					oldx = marux;
					oldy = maruy;
				}

			} else if (stage == 2) {
				/* 迷路を解いている途中 */
				repaint();

				/* 30ミリ秒待機する */
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
				}

				move2();

				if ((marux == MAZE_WIDTH - 2) && (maruy == MAZE_HEIGHT - 2)) {
					/* ゴールに着いたら地図初期化へ */
					stage = 3;
				}
			} else if (stage == 3) {
				repaint();

				/* 5秒待機する */
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				stage = 0;
			}
		}
	}

	private void move() {
		/* まず左に行けるかどうかチェック */
		int nextd = (marud + 3) % 4;
		int next = block[maruy + dy[nextd]][marux + dx[nextd]];
		if (next != 1) {
			marud = nextd;

			oldx = marux;
			oldy = maruy;
			marux += dx[marud];
			maruy += dy[marud];
			block[maruy][marux] = 2;

			return;
		}

		/* 次に現在の進行方向に行けるかチェック */
		next = block[maruy + dy[marud]][marux + dx[marud]];

		while (next == 1) {
			/* 行けなければ右へ右へと向きを変えて行けるかどうかチェック */
			marud = (marud + 1) % 4;
			next = block[maruy + dy[marud]][marux + dx[marud]];
		}

		oldx = marux;
		oldy = maruy;
		marux += dx[marud];
		maruy += dy[marud];
		block[maruy][marux] = 2;
	}

	private void move2() {
		/* まず右に行けるかどうかチェック */
		int nextd = (marud + 1) % 4;
		;
		int next = block[maruy + dy[nextd]][marux + dx[nextd]];

		if (next != 1) {
			marud = nextd;

			oldx = marux;
			oldy = maruy;
			marux += dx[marud];
			maruy += dy[marud];

			if (next == 2)
				block[maruy][marux] = 3;
			else if (next != 3)
				block[maruy][marux] = 4;
			return;
		}

		/* 次に現在の進行方向に行けるかチェック */
		next = block[maruy + dy[marud]][marux + dx[marud]];

		while (next == 1) {
			/* 行けなければ左へ向きを変えて行けるかどうかチェック */
			marud = (marud + 3) % 4;
			next = block[maruy + dy[marud]][marux + dx[marud]];
		}

		oldx = marux;
		oldy = maruy;
		marux += dx[marud];
		maruy += dy[marud];
		if (next == 2)
			block[maruy][marux] = 3;
		else if (next != 3)
			block[maruy][marux] = 4;
	}

}
