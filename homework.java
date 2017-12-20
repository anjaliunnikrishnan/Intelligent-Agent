import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class homework {
    private static long start;
    private Queue<Node> q = new LinkedList<>();
    private Queue<Node> q2 = new LinkedList<>();
    private static PrintWriter writer;
    private int flag = -1;
   // private Node state = new Node();
    private double TIME_LIMIT = 10;
    private int DEPTH = 3;
    private int score;

    public static void main(String[] args) throws Exception {
        homework hw = new homework();
        FileReader fr = new FileReader("input.txt");
        writer = new PrintWriter("output.txt");
        BufferedReader br = new BufferedReader(fr);
        int grid_size = Integer.parseInt(br.readLine());
        int fruit_types = Integer.parseInt(br.readLine());
        double time_left = Double.parseDouble(br.readLine());
        char candy_grid[][] = new char[grid_size][grid_size];
        int row = 0, col;
        String line = "";
        while ((line = br.readLine()) != null) {
            char[] chars = line.toCharArray();
            for (col = 0; col < chars.length; col++) {
                candy_grid[row][col] = chars[col];
            }
            row++;
        }
        br.close();
        start = System.currentTimeMillis();
        Grid parent = new Grid(candy_grid, 0, Double.NEGATIVE_INFINITY);
        Grid g = new Grid(candy_grid, 0, Double.NEGATIVE_INFINITY);
        if(time_left<1)
            hw.DEPTH = 1;
        else if(time_left <hw.TIME_LIMIT) {
            //  hw.DEPTH -= 1;
            hw.TIME_LIMIT = time_left;
        }
        Node n = hw.minimax(g);
        long end = System.currentTimeMillis();
        //System.out.println("Time Taken " + (end - start));
        hw.write_to_output_file(n, parent);
    }

    private Grid place_asterisk(Grid g, int row, int col, int depth) throws IOException {
        int count = 0;
        Node n;
        Node n2 = new Node();
        n2.row = row;
        n2.col = col;
        add_move_to_queue(n2.row, n2.col);
        char fruit_type = g.candy[n2.row][n2.col];
        while (!q.isEmpty() && fruit_type != '*') {
            n = q.element();
            row = n.row;
            col = n.col;

            if (((col - 1) >= 0) && g.candy[row][col - 1] == fruit_type) {
                add_move_to_queue(row, col - 1);
                g.candy[row][col - 1] = '*';
            }
            if (((col + 1) < g.candy[0].length) && g.candy[row][col + 1] == fruit_type) {
                add_move_to_queue(row, col + 1);
                g.candy[row][col + 1] = '*';
            }

            if (((row - 1) >= 0) && g.candy[row - 1][col] == fruit_type) {
                add_move_to_queue(row - 1, col);
                g.candy[row - 1][col] = '*';
            }
            if (((row + 1) < g.candy[0].length) && g.candy[row + 1][col] == fruit_type) {
                add_move_to_queue(row + 1, col);
                g.candy[row + 1][col] = '*';
            }
            g.candy[row][col] = '*';
            count++;
            q.remove();
        }
        score = count * count;
        //hm.put(n2, score);
        if (flag == 1) {
            g.score += score;
        } else if (flag == 0) {
            g.score -= score;
        }
        return g;
    }

    private void add_move_to_queue(int row, int col) throws IOException {
        Node node = new Node();
        node.row = row;
        node.col = col;
        q2.add(node);
        q.add(node);
    }

    private void apply_gravity(char[][] candy_grid) throws IOException {
        Node n;
        int pointer, k;
        while (!q2.isEmpty()) {
            n = q2.element();
            pointer = 1;
            k = n.row;
            while ((k - pointer) >= 0) {
                if (candy_grid[k - pointer][n.col] == '*') {
                    pointer++;
                    continue;
                }
                if (candy_grid[k][n.col] == '*') {
                    char temp = candy_grid[k][n.col];
                    candy_grid[k][n.col] = candy_grid[k - pointer][n.col];
                    candy_grid[k - pointer][n.col] = temp;
                }
                k = k - pointer;
                pointer = 1;
            }
            q2.remove();
        }
    }

    private ArrayList<Node> next_legal_move(Grid g) throws Exception {
        ArrayList<Node> ar = new ArrayList<>();
        int row = 0, col = 0;
        while (row < g.candy[0].length) {
            if (g.candy[row][col] != '*') {
                Node n = new Node();
                n.row = row;
                n.col = col;
                g = place_asterisk(g, row, col, 0);
                n.score = score;
                ar.add(n);
            }
            if (col + 1 >= g.candy[0].length) {
                row = row + 1;
                col = 0;
            } else
                col = col + 1;
        }
        return ar;
    }

    private Node minimax(Grid g2) throws Exception {
        Node state = new Node();
        Grid parent = new Grid(g2.candy, g2.score, g2.v);
        int depth = 0, alpha = -999999, beta = 9999999;
        HashMap<Node, Double> hm = new HashMap<>();
        ArrayList<Node> ar = next_legal_move(g2);
        while (!ar.isEmpty()){
            Node n = get_highest_move(ar);
            Grid g = new Grid(parent.candy, parent.score, Double.POSITIVE_INFINITY);
            flag = 1;
            g = place_asterisk(g, n.row, n.col, depth);
            flag = -1;
            apply_gravity(g.candy);
            g.v = min_value(g, alpha, beta, depth + 1);
            hm.put(n, g.v);
            long end = System.currentTimeMillis();
            if((end-start)>=TIME_LIMIT*1000){
                state = return_move(hm);
                return state;
            }
        }
        state = return_move(hm);
        return state;
    }

    private Node return_move(HashMap<Node, Double> hm){
        Double initial_value = Double.NEGATIVE_INFINITY;
        Node state = new Node();
        for (Node n : hm.keySet()) {
            Double value = hm.get(n);
            if (value > initial_value) {
                initial_value = value;
                state = n;
            }
        }
        return state;
    }

    private void write_to_output_file(Node n, Grid parent) throws Exception {
        if (n.row != -1 || n.col != -1) {
            char c = (char) (n.col + 'A');
            //System.out.println("Move Made: " + c + (n.row + 1));
            parent = place_asterisk(parent, n.row, n.col, 0);
            apply_gravity(parent.candy);
            writer.print(c);
            writer.println(n.row + 1);
            print_grid(parent.candy);
        }
        writer.flush();
        writer.close();
        System.exit(0);
    }
    private Node get_highest_move(ArrayList<Node> ar){
        int curr_score = -1, pos = -1;
        for(int i=0; i<ar.size(); i++){
            if(ar.get(i).score>curr_score){
                curr_score = ar.get(i).score;
                pos = i;
            }
        }
        Node n = ar.get(pos);
        ar.remove(pos);
        return n;
    }

    private double max_value(Grid state, int alpha, int beta, int depth) throws Exception {
        long end = System.currentTimeMillis();
        Grid grid = new Grid(state.candy, state.score, state.v);
        if (check_star_count(grid.candy) == grid.candy[0].length * grid.candy[0].length || depth == DEPTH
                || (end-start)>=TIME_LIMIT*1000){
            grid.v = grid.score;
            return grid.v;
        }
        ArrayList<Node> ar = next_legal_move(state);
        while (!ar.isEmpty()){
            end = System.currentTimeMillis();
            if((end-start)>=TIME_LIMIT*1000){
                grid.v = grid.score;
                return grid.v;
            }
            Node node = get_highest_move(ar);
            Grid g = new Grid(grid.candy, grid.score, Double.POSITIVE_INFINITY);//check if positive
            flag = 1;
            g = place_asterisk(g, node.row, node.col, depth);
            flag = -1;
            apply_gravity(g.candy);
            grid.v = Math.max(grid.v, min_value((g), alpha, beta, depth + 1));
            if (grid.v >= beta) return grid.v;
            alpha = Math.max(alpha, (int) grid.v);
        }
        return grid.v;
    }

    private double min_value(Grid state, int alpha, int beta, int depth) throws Exception {
        long end = System.currentTimeMillis();
        Grid parent = new Grid(state.candy, state.score, state.v);
        if (check_star_count(parent.candy) == parent.candy[0].length * parent.candy[0].length || depth == DEPTH
                || (end-start)>=TIME_LIMIT*1000){
            parent.v = parent.score;
            return parent.v;
        }
        ArrayList<Node> ar = next_legal_move(state);
        while (!ar.isEmpty()){
            end = System.currentTimeMillis();
            if((end-start)>=TIME_LIMIT*1000){
                parent.v = parent.score;
                return parent.v;
            }
            Node node = get_highest_move(ar);
            Grid g = new Grid(parent.candy, parent.score, Double.NEGATIVE_INFINITY);
            flag = 0;
            g = place_asterisk(g, node.row, node.col, depth);
            flag = -1;
            apply_gravity(g.candy);
            parent.v = Math.min(parent.v, max_value((g), alpha, beta, depth + 1));
            if (parent.v <= alpha) return parent.v;
            beta = Math.min(beta, (int) parent.v);
        }
        return parent.v;
    }

    private static void print_grid(char[][] candy_grid) throws IOException {
        for (int row = 0; row < candy_grid[0].length; row++) {
            for (int col = 0; col < candy_grid[0].length; col++) {
                writer.print(candy_grid[row][col]);
            }
            writer.println();
        }
        writer.println();
    }

    char[][] create_grid(char[][] c, char[][] o) {
        for (int i = 0; i < c[0].length; i++) {
            System.arraycopy(c[i], 0, o[i], 0, c[0].length);
        }
        return o;
    }

    private int check_star_count(char[][] candy_grid) {
        int star_count = 0;
        for (int i = 0; i < candy_grid[0].length; i++) {
            for (int j = 0; j < candy_grid[0].length; j++) {
                if (candy_grid[i][j] == '*') {
                    star_count++;
                }
            }
        }
        return star_count;
    }
}



class Grid {
    char[][] candy;
    int score;
    double v;

    Grid(char[][] candy_grid, int s, double v){
        candy = new char[candy_grid[0].length][candy_grid[0].length];
        homework crush = new homework();
        candy = crush.create_grid(candy_grid, candy);
        this.score = s;
        this.v = v;
    }
}

class Node {
    int row;
    int col;
    int score;
    Node(){
        row = -1;
        col = -1;
    }
}
