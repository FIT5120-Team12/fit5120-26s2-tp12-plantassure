import torch

print("PyTorch:", torch.__version__)

print("MPS available:", torch.backends.mps.is_available())

if torch.backends.mps.is_available():
    device = torch.device("mps")
else:
    device = torch.device("cpu")

print("Device:", device)

x = torch.rand(3, 3).to(device)

print(x)