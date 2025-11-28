import pandas as pd
import matplotlib.pyplot as plt
import os
import sys

# -----------------------------------------
# Folder structure
# -----------------------------------------
CSV_DIR = "csv_files/interoperability_csv"
GRAPH_MAIN = "graphs/interoperability"

os.makedirs(GRAPH_MAIN, exist_ok=True)

# -----------------------------------------
# Helper: plot lines only when columns exist
# -----------------------------------------
def maybe_plot(df, col, label):
    if col in df.columns:
        plt.plot(df["t"], df[col], label=label)   # <-- FIXED: use normalized time
    else:
        print(f"⚠ Skipping missing column: {col}")

# -----------------------------------------
# Gather CSV files
# -----------------------------------------
csv_files = [f for f in os.listdir(CSV_DIR) if f.endswith(".csv")]

if not csv_files:
    print("❌ No CSV files found in", CSV_DIR)
    sys.exit(1)

print("Found CSV files:", csv_files)

# -----------------------------------------
# Process each CSV file
# -----------------------------------------
for file in csv_files:

    csv_path = os.path.join(CSV_DIR, file)
    base = os.path.splitext(file)[0]
    endpoint = base.replace("_metrics", "")

    OUT_DIR = os.path.join(GRAPH_MAIN, endpoint)
    os.makedirs(OUT_DIR, exist_ok=True)

    print(f"\n📊 Processing {file} → saving graphs into {OUT_DIR}/")

    # Load CSV
    df = pd.read_csv(csv_path)

    # -----------------------------------------
    # Normalize timestamp to start at 0
    # -----------------------------------------
    df["t"] = df["timestamp"] - df["timestamp"].min()

    # ===============================================================
    # 1. Transaction Time Graph (Add / Update / Delete)
    # ===============================================================
    plt.figure(figsize=(12,6))

    maybe_plot(df, "add_ms",    "Add")
    maybe_plot(df, "update_ms", "Update")
    maybe_plot(df, "delete_ms", "Delete")

    plt.title(f"{endpoint} — Transaction Time (ms)")
    plt.xlabel("Time Offset (ms)")
    plt.ylabel("Time (ms)")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT_DIR}/{endpoint}_transaction.png", dpi=300)
    plt.close()

    # ===============================================================
    # 2. CPU Usage Graph (Add / Update / Delete)
    # ===============================================================
    plt.figure(figsize=(12,6))

    maybe_plot(df, "add_cpu",    "CPU Add")
    maybe_plot(df, "update_cpu", "CPU Update")
    maybe_plot(df, "delete_cpu", "CPU Delete")

    plt.title(f"{endpoint} — CPU Usage (%)")
    plt.xlabel("Time Offset (ms)")
    plt.ylabel("CPU (%)")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT_DIR}/{endpoint}_cpu.png", dpi=300)
    plt.close()

    # ===============================================================
    # 3. Free Memory Graph (Add / Update / Delete)
    # ===============================================================
    plt.figure(figsize=(12,6))

    maybe_plot(df, "add_mem",    "Memory Add")
    maybe_plot(df, "update_mem", "Memory Update")
    maybe_plot(df, "delete_mem", "Memory Delete")

    plt.title(f"{endpoint} — Free Memory (MB)")
    plt.xlabel("Time Offset (ms)")
    plt.ylabel("Memory (MB)")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT_DIR}/{endpoint}_memory.png", dpi=300)
    plt.close()

    print(f"✔ Finished {endpoint} — graphs saved.")

print("\n🎉 All graphs generated successfully!")
print(f"📁 Location: {GRAPH_MAIN}/")
