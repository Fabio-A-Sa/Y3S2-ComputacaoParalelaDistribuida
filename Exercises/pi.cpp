//#include <omp.h>
#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <math.h>

using namespace std;

int main (int argc, char *argv[])
{

	double area, pi, x;
	int i, n;
	char c;
	
	//cout << "Number of processors: " << omp_get_num_procs() << endl;

	cout << "Number of divisions ? "; 
	cin >> n; 
	area = 0.0;

	//#pragma omp parallel for private(x) reduction(+:area)
	for (i = 0; i < n; i++) {
	   x = (i+0.5)/n;
	   area += 4.0/(1.0 + x*x);
	}
	pi = area / n;

	cout << setprecision(18) << "PI = " << pi << endl << endl;
	cout << setprecision(18) << "Error = " << 4.0*atan(1) - pi << endl << endl;
	
	cout << "Enter to continue ...";
	cin.get(c);
	cin.get(c);
	
}