#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <fstream>
#include <string>

using namespace std;
#define SYSTEMTIME clock_t

void OnMult(int m_ar, int m_br) 
{

	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

void OnMultLine(int m_ar, int m_br)
{       
    SYSTEMTIME Time1, Time2;
    
    char st[100];
    int i, j, k;

    double *pha, *phb, *phc;
    
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);
    
    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;

    Time1 = clock();

    for(i=0; i<m_ar; i++)
    {    for( k=0; k<m_ar; k++ )
        { for( j=0; j<m_br; j++)
            {    
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
            }
        }
    }

    Time2 = clock();
    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    // display 10 elements of the result matrix tto verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {    for(j=0; j<min(10,m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

void OnMultBlock(int m_ar, int m_br, int bkSize)
{
    SYSTEMTIME Time1, Time2;
    
    char st[100];
    int i, j, k, ii, jj, kk;

    double *pha, *phb, *phc;
    
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);
    
    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;

    Time1 = clock();

    
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

	Time2 = clock();
    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
    cout << st;

    // display 10 elements of the result matrix to verify correctness
    cout << "Result matrix: " << endl;
    for(i=0; i<1; i++)
    {    for(j=0; j<min(10,m_br); j++)
            cout << phc[j] << " ";
    }
    cout << endl;

    free(pha);
    free(phb);
    free(phc);
}

void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

int main (int argc, char *argv[])
{	
	int EventSet = PAPI_NULL;
  	long long values[3];
  	int ret;
	
	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;

	ret = PAPI_create_eventset(&EventSet);
	if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

    ret = PAPI_add_event(EventSet,PAPI_L2_DCA);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCA" << endl;

    int op = atoi(argv[1]);
    ofstream f;
    f.open(op == 3 ? argv[4] : argv[3], ios::app);
    int matrix_size = atoi(argv[2]), block_size = 0;

    // Start counting
	ret = PAPI_start(EventSet);
	if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

	switch (op){
		case 1: 
			OnMult(matrix_size, matrix_size);
			break;
		case 2:
			OnMultLine(matrix_size, matrix_size);  
			break;
		case 3:
            block_size = atoi(argv[3]);
			OnMultBlock(matrix_size, matrix_size, block_size);  
			break;
        default:
            return 1;
	}

    // Stop counting
  	ret = PAPI_stop(EventSet, values);
  	if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

    // Show results
	printf("L1 DCM: %lld \n",values[0]);
	printf("L2 DCM: %lld \n",values[1]);
    printf("L2 DCA: %lld \n",values[2]);

    // Write data
    f << "Dimensions: " << matrix_size << "\n";
    if (op == 3)
        f << "Block Size: " << block_size << "\n";
    f << "L1 DCM:" <<  values[0] << "\n";
    f << "L2 DCM:" <<  values[1] << "\n";
    f << "L2 DCA:" <<  values[2] << "\n";

	ret = PAPI_reset( EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL reset" << endl; 

    f.close();

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

    ret = PAPI_remove_event( EventSet, PAPI_L2_DCA );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

    return 0;
}
