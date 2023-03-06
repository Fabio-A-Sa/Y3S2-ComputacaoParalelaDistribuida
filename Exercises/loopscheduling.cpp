#include <omp.h>
#include <iostream>
#include <unistd.h>

using namespace std;

int main (int argc, char *argv[])
{	int n = 10;
	int i;

	#pragma omp parallel num_threads(4)
	{
		#pragma omp master	
		{	cout << endl << "my thread: " << omp_get_thread_num() << endl;
			cout << "Number of threads: " << omp_get_num_threads() << endl;
		}
		
		#pragma omp for schedule (static, 4)		// try (dynamic, 4)   (dynamic, 1)
		for (i = 0; i < n; i++) {
			// to not overlap the output
			//#pragma omp critical
			cout  << "Thread: " << omp_get_thread_num() << "   i= " << i << endl;
			usleep(1000*1000*omp_get_thread_num());
		}


	}

	return 0;
}