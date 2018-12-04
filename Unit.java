// ゲームのフィールド上に存在するあたり判定のあるもののスーパークラスとなる
public abstract class Unit extends Object
{
	private boolean action_flag; // ターン中のアクションの有無
        private int speed; // 速さ
        private int maxHp; // 体力,0になると消滅
	private int hp;
        private int attack; // 攻撃力
        private int view; // 視界
      	private int dir; // 向いている方向, テンキーに倣う
	// 7 8 9
	// 4 5 6
	// 1 2 3

        // 移動  抽象メソッド,サブクラスにより定義
	abstract void moveobj();

	// 引数のユニットから攻撃されたときのダメージ計算
	public void damageCalc(Unit u, Background bg)
	{
		// hpから攻撃力分のダメージを引く
		hp -= u.attack;

		if(hp <= 0) {
			hp = 0;
			active = false;
			//Background.mapUnit[gridMapY][gridMapX] = -1;
			bg.setMapUnit(gridMapX, gridMapY, -1);
			//Game.appendRog("beat " + getName());
		}
	}

	// 引数のユニットから攻撃されたときのダメージ計算
	public void damageCalc(Unit u, Info info)
	{
		// hpから攻撃力分のダメージを引く
		hp -= u.attack;

		if(hp <= 0) {
			hp = 0;
			active = false;
			//bgSimu.mapUnit[gridMapY][gridMapX] = -1;
			info.mapUnit[gridMapY][gridMapX] = -1;
			//Game.appendRog("beat " + getName());
		}
	}

	// 攻撃されたときのダメージ計算
	public void damageCalc(int dam, Background bg)
	{
		// hpから攻撃力分のダメージを引く
		hp -= dam;

		if(hp <= 0) {
			hp = 0;
			active = false;

			//Background.mapUnit[gridMapY][gridMapX] = -1;
			bg.setMapUnit(gridMapX, gridMapY, -1);
			//Game.appendRog("beat " + getName());
		}
	}

	public void damageCalc(int dam, Info info)
	{
		// hpから攻撃力分のダメージを引く
		hp -= dam;

		if(hp <= 0) {
			hp = 0;
			active = false;

			//bgSimu.mapUnit[gridMapY][gridMapX] = -1;
			info.mapUnit[gridMapY][gridMapX] = -1;
			//Game.appendRog("beat " + getName());
		}
	}

	abstract public boolean isNextMoveCheck(int nx, int ny);

	// 斜め移動の可否
	// 真：通行不可
	// 偽：通行可
	public boolean isDiagonalMoveCheck(int mx, int my, Background bg)
	{
		//
		if(//Background.map[gridMapY][gridMapX + mx] == 1 ||
		   bg.getMap(gridMapX + mx, gridMapY) == 1 ||
		   //Background.map[gridMapY + my][gridMapX] == 1)
		   bg.getMap(gridMapX, gridMapY + my) == 1){
			return true;
		}
                else {
			return false;
		}
	}

	// 斜め攻撃の可否
	public boolean isDiagonalAtkCheck(int mx, int my, Background bg)
	{
		if(//Background.map[gridMapY][gridMapX + mx] == 1 ||
		   bg.getMap(gridMapX + mx, gridMapY) == 1 ||
		   //Background.map[gridMapY + my][gridMapX] == 1)
		   bg.getMap(gridMapX, gridMapY + my) == 1) {
			return true;
		}
                else {
			return false;
		}
	}
        
        
        // getter,setter
        
        public boolean isActionFlag(){ return this.action_flag; }
        public void setActionFlag(boolean action_flag){ this.action_flag = action_flag; }
        
        public int getDir(){ return this.dir; }
        public void setDir(int dir){ this.dir = dir; }
        
        public int getSpeed(){ return this.speed; }
        public void setSpeed(int speed){ this.speed = speed; }
        
        public int getMaxHp(){ return this.maxHp; }
        public void setMaxHp(int maxHp){ this.maxHp = maxHp; }
        
        public int getHp(){ return this.hp; }
        public void setHp(int hp){ this.hp = hp; }
        public void addHp(int healVal){ 
            this.hp += healVal; 
            if(this.hp > this.maxHp) this.hp = this.maxHp;
            if(this.hp < 0) this.hp = 0;
        }
        
        public int getAttack(){ return attack; }
        public void setAttack(int attack){ this.attack = attack; }
        
        public int getView(){ return view; }
        public void setView(int view){ this.view = view; }
}
