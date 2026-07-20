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

package com.shatteredpixel.shatteredpixeldungeon.levels.zone;

import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

public class ZoneMapGenerator {

	public static final int MAX_ATTEMPTS = 6;

	private final int width;
	private final int height;

	public ZoneMapGenerator( int width, int height ) {
		if (width < 32 || height < 32) {
			throw new IllegalArgumentException("Zone maps must be at least 32x32 cells");
		}
		this.width = width;
		this.height = height;
	}

	public Result generate() {
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			Result result = generateCandidate();
			if (isConnected(result)) {
				return result;
			}
		}
		Result fallback = generateFallback();
		if (!isConnected(fallback)) {
			throw new IllegalStateException("Zone fallback must keep every required target connected");
		}
		return fallback;
	}

	private Result generateCandidate() {
		int[] map = new int[width * height];
		Arrays.fill(map, Terrain.GRASS);

		paintOpenFields(map);
		paintWildAreas(map);
		paintBoundary(map);

		int[] roadY = carveMainRoad(map);
		int entrance = cell(2, roadY[2]);
		int exit = cell(width - 3, roadY[width - 3]);

		ArrayList<Integer> plotEntrances = addBuildingPlots(map, roadY);
		map[entrance] = Terrain.ENTRANCE;
		map[exit] = Terrain.EXIT;

		return new Result(map, entrance, exit, plotEntrances);
	}

	private void paintOpenFields( int[] map ) {
		for (int i = 0; i < 12; i++) {
			paintEllipse(map,
					Random.IntRange(5, width - 6),
					Random.IntRange(5, height - 6),
					Random.IntRange(4, 9),
					Random.IntRange(3, 7),
					Terrain.EMPTY);
		}
	}

	private void paintWildAreas( int[] map ) {
		for (int i = 0; i < 10; i++) {
			paintEllipse(map,
					Random.IntRange(4, width - 5),
					Random.IntRange(4, height - 5),
					Random.IntRange(3, 7),
					Random.IntRange(3, 7),
					Terrain.HIGH_GRASS);
		}

		for (int i = 0; i < 4; i++) {
			paintEllipse(map,
					Random.IntRange(6, width - 7),
					Random.IntRange(6, height - 7),
					Random.IntRange(2, 5),
					Random.IntRange(2, 4),
					Terrain.WATER);
		}

		for (int i = 0; i < 5; i++) {
			paintEllipse(map,
					Random.IntRange(5, width - 6),
					Random.IntRange(5, height - 6),
					Random.IntRange(2, 4),
					Random.IntRange(2, 4),
					Terrain.WALL);
		}
	}

	private void paintEllipse( int[] map, int centerX, int centerY,
			int radiusX, int radiusY, int terrain ) {
		for (int y = Math.max(1, centerY - radiusY); y <= Math.min(height - 2, centerY + radiusY); y++) {
			for (int x = Math.max(1, centerX - radiusX); x <= Math.min(width - 2, centerX + radiusX); x++) {
				int dx = x - centerX;
				int dy = y - centerY;
				if (dx * dx * radiusY * radiusY + dy * dy * radiusX * radiusX
						<= radiusX * radiusX * radiusY * radiusY) {
					map[cell(x, y)] = terrain;
				}
			}
		}
	}

	private void paintBoundary( int[] map ) {
		for (int x = 0; x < width; x++) {
			map[cell(x, 0)] = Terrain.WALL;
			map[cell(x, height - 1)] = Terrain.WALL;
		}
		for (int y = 0; y < height; y++) {
			map[cell(0, y)] = Terrain.WALL;
			map[cell(width - 1, y)] = Terrain.WALL;
		}
	}

	private int[] carveMainRoad( int[] map ) {
		int[] roadY = new int[width];
		int y = height / 2 + Random.IntRange(-3, 3);
		for (int x = 1; x < width - 1; x++) {
			if (x % 6 == 0) {
				y = clamp(y + Random.IntRange(-1, 1), height / 2 - 5, height / 2 + 5);
			}
			roadY[x] = y;
			carveRoadCell(map, x, y, 1);
		}
		return roadY;
	}

	private ArrayList<Integer> addBuildingPlots( int[] map, int[] roadY ) {
		int plotCount = Random.IntRange(3, 4);
		ArrayList<Integer> entrances = new ArrayList<>(plotCount);
		int spacing = (width - 20) / (plotCount - 1);

		for (int i = 0; i < plotCount; i++) {
			int branchX = 10 + i * spacing;
			int plotWidth = Random.IntRange(6, 8);
			int plotHeight = Random.IntRange(5, 7);
			int centerX = clamp(branchX + Random.IntRange(-4, 4), 6, width - 7);
			int left = clamp(centerX - plotWidth / 2, 3, width - plotWidth - 3);
			boolean aboveRoad = i % 2 == 0;
			int top;
			if (aboveRoad) {
				top = Random.IntRange(5, 12);
			} else {
				top = height - plotHeight - Random.IntRange(5, 12);
			}

			paintPlot(map, left, top, plotWidth, plotHeight);
			int entranceX = left + plotWidth / 2;
			int entranceY = aboveRoad ? top + plotHeight - 1 : top;
			int plotEntrance = cell(entranceX, entranceY);
			entrances.add(plotEntrance);

			carveBranch(map, branchX, roadY[branchX], entranceX, entranceY);
			clearPlotApproach(map, entranceX, entranceY, aboveRoad);
		}

		return entrances;
	}

	private void paintPlot( int[] map, int left, int top, int plotWidth, int plotHeight ) {
		for (int y = top; y < top + plotHeight; y++) {
			for (int x = left; x < left + plotWidth; x++) {
				map[cell(x, y)] = Terrain.EMPTY_DECO;
			}
		}
	}

	private void carveBranch( int[] map, int startX, int startY, int endX, int endY ) {
		int bendY = (startY + endY) / 2;
		carveVertical(map, startX, startY, bendY);
		carveHorizontal(map, startX, endX, bendY);
		carveVertical(map, endX, bendY, endY);
	}

	private void carveVertical( int[] map, int x, int fromY, int toY ) {
		int start = Math.min(fromY, toY);
		int end = Math.max(fromY, toY);
		for (int y = start; y <= end; y++) {
			carveRoadCell(map, x, y, 0);
			carveRoadCell(map, x + 1, y, 0);
		}
	}

	private void carveHorizontal( int[] map, int fromX, int toX, int y ) {
		int start = Math.min(fromX, toX);
		int end = Math.max(fromX, toX);
		for (int x = start; x <= end; x++) {
			carveRoadCell(map, x, y, 0);
			carveRoadCell(map, x, y + 1, 0);
		}
	}

	private void carveRoadCell( int[] map, int x, int y, int radius ) {
		for (int yy = y - radius; yy <= y + radius; yy++) {
			for (int xx = x - radius; xx <= x + radius; xx++) {
				if (xx > 0 && xx < width - 1 && yy > 0 && yy < height - 1) {
					map[cell(xx, yy)] = Terrain.EMPTY_SP;
				}
			}
		}
	}

	private void clearPlotApproach( int[] map, int x, int y, boolean aboveRoad ) {
		int direction = aboveRoad ? 1 : -1;
		for (int distance = 0; distance <= 2; distance++) {
			for (int xx = x - 1; xx <= x + 1; xx++) {
				map[cell(xx, y + direction * distance)] = Terrain.EMPTY_SP;
			}
		}
	}

	private boolean isConnected( Result result ) {
		boolean[] reached = new boolean[result.map.length];
		ArrayDeque<Integer> pending = new ArrayDeque<>();
		reached[result.entrance] = true;
		pending.add(result.entrance);

		while (!pending.isEmpty()) {
			int current = pending.removeFirst();
			int x = current % width;
			int y = current / width;
			visit(result.map, reached, pending, x - 1, y);
			visit(result.map, reached, pending, x + 1, y);
			visit(result.map, reached, pending, x, y - 1);
			visit(result.map, reached, pending, x, y + 1);
		}

		if (!reached[result.exit]) {
			return false;
		}
		for (int plotEntrance : result.plotEntrances) {
			if (!reached[plotEntrance]) {
				return false;
			}
		}
		return true;
	}

	private void visit( int[] map, boolean[] reached, ArrayDeque<Integer> pending, int x, int y ) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return;
		}
		int cell = cell(x, y);
		if (!reached[cell] && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0) {
			reached[cell] = true;
			pending.addLast(cell);
		}
	}

	private Result generateFallback() {
		int[] map = new int[width * height];
		Arrays.fill(map, Terrain.GRASS);
		paintEllipse(map, width / 4, height / 4, 6, 4, Terrain.HIGH_GRASS);
		paintEllipse(map, 3 * width / 4, 3 * height / 4, 5, 4, Terrain.WATER);
		paintBoundary(map);

		int roadY = height / 2;
		for (int x = 1; x < width - 1; x++) {
			carveRoadCell(map, x, roadY, 1);
		}

		ArrayList<Integer> plotEntrances = new ArrayList<>(3);
		int[] branchX = {width / 4, width / 2, 3 * width / 4};
		for (int i = 0; i < branchX.length; i++) {
			boolean aboveRoad = i != 1;
			int top = aboveRoad ? 7 : height - 13;
			int left = branchX[i] - 3;
			paintPlot(map, left, top, 7, 6);
			int entranceY = aboveRoad ? top + 5 : top;
			carveBranch(map, branchX[i], roadY, branchX[i], entranceY);
			clearPlotApproach(map, branchX[i], entranceY, aboveRoad);
			plotEntrances.add(cell(branchX[i], entranceY));
		}

		int entrance = cell(2, roadY);
		int exit = cell(width - 3, roadY);
		map[entrance] = Terrain.ENTRANCE;
		map[exit] = Terrain.EXIT;
		return new Result(map, entrance, exit, plotEntrances);
	}

	private int cell( int x, int y ) {
		return x + y * width;
	}

	private int clamp( int value, int min, int max ) {
		return Math.max(min, Math.min(max, value));
	}

	public static class Result {

		public final int[] map;
		public final int entrance;
		public final int exit;
		public final ArrayList<Integer> plotEntrances;

		private Result( int[] map, int entrance, int exit, ArrayList<Integer> plotEntrances ) {
			this.map = map;
			this.entrance = entrance;
			this.exit = exit;
			this.plotEntrances = plotEntrances;
		}
	}
}
