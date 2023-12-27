import numpy as np

# Define a square matrix
A = np.array([[7, 2,1], [0, 3, -1], [-3,4,-2]])

# Calculate the inverse of the matrix
A_inv = np.linalg.inv(A)

print(A_inv)
