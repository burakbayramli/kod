import subprocess
import re

try:
    # Find the correct battery device path
    result_enumerate = subprocess.run(['upower', '-e'], capture_output=True, text=True, check=True)
    battery_device_path = None
    for line in result_enumerate.stdout.splitlines():
        if 'battery_BAT' in line:
            battery_device_path = line.strip()
            break

    if battery_device_path:
        # Get detailed information for the identified battery
        result_info = subprocess.run(['upower', '-i', battery_device_path], capture_output=True, text=True, check=True)
        output = result_info.stdout

        # Extract information using regular expressions
        percentage_match = re.search(r'percentage:\s*(\d+)%', output)
        status_match = re.search(r'state:\s*(\w+)', output)
        energy_full_match = re.search(r'energy-full:\s*([\d.]+)\s*Wh', output)
        energy_full_design_match = re.search(r'energy-full-design:\s*([\d.]+)\s*Wh', output)

        if percentage_match:
            print(f"Current battery percentage: {percentage_match.group(1)}%")
        if status_match:
            print(f"Battery status: {status_match.group(1)}")
        if energy_full_match and energy_full_design_match:
            energy_full = float(energy_full_match.group(1))
            energy_full_design = float(energy_full_design_match.group(1))
            if energy_full_design > 0:
                health_percentage = (energy_full / energy_full_design) * 100
                print(f"Battery health: {health_percentage:.2f}% of original design capacity (based on Wh)")
        elif energy_full_match:
            print(f"Energy Full (Wh): {energy_full_match.group(1)}")
        elif energy_full_design_match:
            print(f"Energy Full Design (Wh): {energy_full_design_match.group(1)}")


    else:
        print("No battery device found using upower -e.")

except FileNotFoundError:
    print("The 'upower' command was not found. Please ensure it is installed.")
except subprocess.CalledProcessError as e:
    print(f"Error running upower command: {e}")
    print(f"Stdout: {e.stdout}")
    print(f"Stderr: {e.stderr}")
except Exception as e:
    print(f"An unexpected error occurred: {e}")
