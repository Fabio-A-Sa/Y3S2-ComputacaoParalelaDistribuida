
import java.util.Scanner;
public class Main {
    public static double OnMult(int m_ar, int m_br) {

        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];

        for (int a = 0; a < m_br; a++) {
            for (int b = 0; b < m_br; b++) {
                phb[a * m_br + b] = a + 1;
            }
        }

        long Time1 = System.currentTimeMillis();

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_br; j++) {
                double temp = 0;
                for (int k = 0; k < m_ar; k++) {
                    temp += pha[i * m_ar + k] * phb[k * m_br + j];
                }
                phc[i * m_ar + j] = temp;
            }
        }

        long Time2 = System.currentTimeMillis();

        double Total_time = (Time2 - Time1) / 1000.0;
        System.out.printf("Time: %.3f seconds%n%n", Total_time);
        System.out.println("Result Matrix: ");
        for (int c = 0; c < Math.min(10, m_br); c++) {
            System.out.printf("%.2f ", phc[c]);
        }
        return Total_time;
    }

    public static double OnMultLine(int m_ar, int m_br) {
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];

        for (int a = 0; a < m_br; a++) {
            for (int b = 0; b < m_br; b++) {
                phb[a * m_br + b] = a + 1;
            }
        }

        long Time1 = System.currentTimeMillis();

        for (int i = 0; i < m_ar; i++) {
            for (int k = 0; k < m_br; k++) {
                for (int j = 0; j < m_ar; j++) {
                    phc[i*m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
                }
            }
        }

        long Time2 = System.currentTimeMillis();

        double Total_time = (Time2 - Time1) / 1000.0;

        System.out.printf("Time: %.3f seconds%n%n", Total_time);
        System.out.println("Result Matrix: ");
        for (int c = 0; c < Math.min(10, m_br); c++) {
            System.out.printf("%.2f ", phc[c]);
        }
        return Total_time;
    }

    public static double OnMultBlock(int m_ar, int m_br, int bkSize) {
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];

        for (int a = 0; a < m_br; a++) {
            for (int b = 0; b < m_br; b++) {
                pha[a * m_br + b] = 1;
                phb[a * m_br + b] = a + 1;
                phc[a * m_br + b] = 0;
            }
        }

        int i, ii, j, jj, k, kk;
        long Time1 = System.currentTimeMillis();

        for(ii=0; ii<m_ar; ii+=bkSize) {
            for( kk=0; kk<m_ar; kk+=bkSize){
                for( jj=0; jj<m_br; jj+=bkSize) {
                    for (i = ii ; i < ii + bkSize ; i++) {
                        for (k = kk ; k < kk + bkSize ; k++) {
                            for (j = jj ; j < jj + bkSize ; j++) {
                                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
                            }
                        }
                    }
                }
            }
        }

        long Time2 = System.currentTimeMillis();

        double Total_time = (Time2 - Time1) / 1000.0;

        System.out.printf("Time: %.3f seconds%n%n", Total_time);
        System.out.println("Result Matrix: ");
        for (int c = 0; c < Math.min(10, m_br); c++) {
            System.out.printf("%.2f ", phc[c]);
        }
        return Total_time;
    }
    public static void main(String[] args) {
        
        if (args.length != 2) return;
        
        int dim = Integer.parseInt(args[0]);
        int bkSize = Integer.parseInt(args[1]);

        double total_time = 0;

        for(int i = 0; i < 5; i++)
            total_time += OnMult(dim, dim);
        total_time /= 5;
        System.out.printf("AVG Time: %.3f seconds%n%n", total_time);

        total_time = 0;
        for(int i = 0; i < 5; i++)
            total_time += OnMultLine(dim, dim);
        total_time /= 5;
        System.out.printf("AVG Time: %.3f seconds%n%n", total_time);

        total_time = 0;

        for(int i = 0; i < 5; i++)
            total_time += OnMultBlock(dim, dim, bkSize);
        total_time /= 5;
        System.out.printf("AVG Time: %.3f seconds%n%n", total_time);
    }
}
