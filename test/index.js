class PharmacySimulation {
    constructor(serviceTime, busyTime, arrivalMin, arrivalMax, customerCount) {
        this.serviceTime = serviceTime; // Time to serve a customer
        this.busyTime = busyTime; // Time the pharmacist is busy after serving
        this.arrivalMin = arrivalMin; // Min time between arrivals
        this.arrivalMax = arrivalMax; // Max time between arrivals
        this.customerCount = customerCount; // Total customers
        this.queue = [];
        this.currentTime = 0;
        this.commands = [];
        this.entityCounter = 0;
        this.pharmacistBusy = false;
        this.customers = [];
        this.generateArrivals();
    }

    logCommand(timestamp, command) {
        this.commands.push(`${timestamp}: ${command}`);
    }

    getRandomArrivalTime() {
        return Math.floor(Math.random() * (this.arrivalMax - this.arrivalMin + 1)) + this.arrivalMin;
    }

    generateArrivals() {
        let arrivalTime = 0;
        for (let i = 0; i < this.customerCount; i++) {
            arrivalTime += this.getRandomArrivalTime();
            this.customers.push(arrivalTime);
        }
    }

    run() {
        for (let arrivalTime of this.customers) {
            let entityId = `entity_${this.entityCounter++}`;

            this.logCommand(arrivalTime, `SPAWN "customer" AS "${entityId}"`);
            this.logCommand(arrivalTime, `QUEUE "initial_queue" JOIN "${entityId}"`);
            this.queue.push({ entityId, arrivalTime });

            if (!this.pharmacistBusy) {
                this.processQueue();
            }
        }

        console.log(this.commands.join("\n"));
    }

    processQueue() {
        while (this.queue.length > 0) {
            let { entityId, arrivalTime } = this.queue.shift();
            let serviceStartTime = Math.max(this.currentTime, arrivalTime);

            this.pharmacistBusy = true;
            this.currentTime = serviceStartTime;

            // Customer leaves queue and pharmacist starts service
            this.logCommand(serviceStartTime, `QUEUE "initial_queue" LEAVE "${entityId}"`);
            this.logCommand(serviceStartTime, `RESOURCE "pharmacist" SET STATE "active"`);

            this.currentTime += this.serviceTime;

            // Pharmacist enters busy state (break time) after service
            this.logCommand(this.currentTime, `RESOURCE "pharmacist" SET STATE "busy"`);

            this.currentTime += this.busyTime;

            // Pharmacist becomes idle after break
            this.logCommand(this.currentTime, `RESOURCE "pharmacist" SET STATE "idle"`);
        }
        this.pharmacistBusy = false;
    }
}

// Parameters: serviceTime, busyTime, arrivalMin, arrivalMax, customerCount
const simulation = new PharmacySimulation(5, 4, 2, 8, 10);
simulation.run();
