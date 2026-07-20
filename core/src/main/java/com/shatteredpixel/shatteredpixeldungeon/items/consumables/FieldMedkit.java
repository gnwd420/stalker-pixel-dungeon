/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.consumables;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class FieldMedkit extends MedicalConsumable {

	{
		image = ItemSpriteSheet.SUPPLY_RATION;
	}

	@Override
	protected boolean canUse( Hero hero ) {
		if (hero.HP >= hero.HT) {
			GLog.w( Messages.get( this, "full_health" ) );
			return false;
		}
		return true;
	}

	@Override
	protected void applyEffect( Hero hero ) {
		if (Dungeon.isChallenged( Challenges.NO_HEALING )) {
			PotionOfHealing.pharmacophobiaProc( hero );
			return;
		}

		int amount = Math.max( 10, Math.round( hero.HT * 0.4f ) );
		amount = Math.min( amount, hero.HT - hero.HP );
		hero.HP += amount;
		hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( amount ), FloatingText.HEALING );
	}

	@Override
	public int value() {
		return 30 * quantity();
	}

}
