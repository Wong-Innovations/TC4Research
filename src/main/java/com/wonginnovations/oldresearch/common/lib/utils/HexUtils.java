package com.wonginnovations.oldresearch.common.lib.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class HexUtils {
    static final int[][] NEIGHBOURS = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};

    public static int getDistance(HexUtils.Hex a1, HexUtils.Hex a2) {
        return (Math.abs(a1.q - a2.q) + Math.abs(a1.r - a2.r) + Math.abs(a1.q + a1.r - a2.q - a2.r)) / 2;
    }

    public static HexUtils.Hex getRoundedHex(double qq, double rr) {
        return getRoundedCubicHex(qq, rr, -qq - rr).toHex();
    }

    public static HexUtils.CubicHex getRoundedCubicHex(double xx, double yy, double zz) {
        int rx = (int)Math.round(xx);
        int ry = (int)Math.round(yy);
        int rz = (int)Math.round(zz);
        double x_diff = Math.abs((double)rx - xx);
        double y_diff = Math.abs((double)ry - yy);
        double z_diff = Math.abs((double)rz - zz);
        if(x_diff > y_diff && x_diff > z_diff) {
            rx = -ry - rz;
        } else if(y_diff > z_diff) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }

        return new HexUtils.CubicHex(rx, ry, rz);
    }

    public static ArrayList<Hex> getRing(int radius) {
        HexUtils.Hex h = new HexUtils.Hex(0, 0);

        for(int k = 0; k < radius; ++k) {
            h = h.getNeighbour(4);
        }

        ArrayList<HexUtils.Hex> ring = new ArrayList();

        for(int i = 0; i < 6; ++i) {
            for(int j = 0; j < radius; ++j) {
                ring.add(h);
                h = h.getNeighbour(i);
            }
        }

        return ring;
    }

    public static ArrayList<HexUtils.Hex> distributeRingRandomly(int radius, int entries, Random random) {
        ArrayList<HexUtils.Hex> ring = getRing(radius);
        ArrayList<HexUtils.Hex> results = new ArrayList();
        float spacing = (float)ring.size() / (float)entries;
        random.nextInt(ring.size());
        float pos = 0.0F;

        for(int i = 0; i < entries; ++i) {
            results.add(ring.get(Math.round(pos)));
            pos += spacing;
        }

        return results;
    }

    public static HashMap<String, Hex> generateHexes(int radius) {
        HashMap<String, HexUtils.Hex> results = new HashMap();
        HexUtils.Hex h = new HexUtils.Hex(0, 0);
        results.put(h.toString(), h);

        for(int k = 0; k < radius; ++k) {
            h = h.getNeighbour(4);
            HexUtils.Hex hd = new HexUtils.Hex(h.q, h.r);

            for(int i = 0; i < 6; ++i) {
                for(int j = 0; j <= k; ++j) {
                    results.put(hd.toString(), hd);
                    hd = hd.getNeighbour(i);
                }
            }
        }

        return results;
    }

    public static class CubicHex {
        public int x = 0;
        public int y = 0;
        public int z = 0;

        public CubicHex(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public HexUtils.Hex toHex() {
            return new HexUtils.Hex(this.x, this.z);
        }
    }

    public static class Hex {
        public int q = 0;
        public int r = 0;

        public Hex(int q, int r) {
            this.q = q;
            this.r = r;
        }

        public HexUtils.CubicHex toCubicHex() {
            return new HexUtils.CubicHex(this.q, this.r, -this.q - this.r);
        }

        public HexUtils.Pixel toPixel(int size) {
            return new HexUtils.Pixel((double)size * 1.5D * (double)this.q, (double)size * Math.sqrt(3.0D) * ((double)this.r + (double)this.q / 2.0D));
        }

        public HexUtils.Hex getNeighbour(int direction) {
            int[] d = HexUtils.NEIGHBOURS[direction];
            return new HexUtils.Hex(this.q + d[0], this.r + d[1]);
        }

        public boolean equals(HexUtils.Hex h) {
            return h.q == this.q && h.r == this.r;
        }

        public String toString() {
            return this.q + ":" + this.r;
        }
    }

    public static class Pixel {
        public double x = 0.0D;
        public double y = 0.0D;

        public Pixel(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public HexUtils.Hex toHex(int size) {
            double qq = 0.6666666666666666D * this.x / (double)size;
            double rr = (0.3333333333333333D * Math.sqrt(3.0D) * -this.y - 0.3333333333333333D * this.x) / (double)size;
            return HexUtils.getRoundedHex(qq, rr);
        }
    }
}
