import time

def OnMult(m_ar, m_br):

    pha = [1.0] * m_ar * m_ar
    phb = [1.0] * m_ar * m_ar
    phc = [1.0] * m_ar * m_ar

    for a in range(m_br):
        for b in range(m_br):
            phb[a*m_br + b] = a+1
    
    Time1 = time.perf_counter()

    for i in range(m_ar):
        for j in range(m_br):
            temp = 0
            for k in range(m_ar):
                temp += pha[i*m_ar+k] * phb[k*m_br+j]
            phc[i*m_ar + j] = temp

    Time2 = time.perf_counter()

    Total_time = Time2 - Time1

    print("Time: {:3.3f} seconds\n".format(Total_time))
    print("Result Matrix: ")
    for c in range(min(10,m_br)):
        print("{} ".format(phc[c]), end="")

print("Dimensions: lins=cols ? ")
dim = int(input())



OnMult(dim, dim)