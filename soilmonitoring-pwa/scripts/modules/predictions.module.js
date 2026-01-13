/**
 * Predictions Module
 * Handles AI predictions display
 */

import { escapeHtml, formatTimeAgo } from './utils.module.js';

/**
 * Display crop prediction
 */
export function displayPrediction(prediction) {
    const container = document.getElementById('predictionsContainer');

    const details = prediction.result.details || [];
    const confidence = (prediction.confidence * 100);

    container.innerHTML = `
        <div class="card p-3 border-success">
            <div class="d-flex align-items-center mb-3">
                <i class="fas fa-seedling fa-2x text-success me-3"></i>
                <div>
                    <h6 class="mb-0">Recommended Crop</h6>
                    <h5 class="mb-0 text-success fw-bold">${escapeHtml(prediction.result.recommendation)}</h5>
                </div>
            </div>
            
            <div class="mb-2">
                <small class="text-muted">Confidence Score</small>
                <div class="progress" style="height: 20px;">
                    <div class="progress-bar bg-success" role="progressbar" 
                         style="width: ${confidence}%" 
                         aria-valuenow="${confidence}" aria-valuemin="0" aria-valuemax="100">
                        ${confidence}%
                    </div>
                </div>
            </div>
            
            <hr>
            
            <div class="mb-2">
                <small class="text-muted"><i class="fas fa-robot me-1"></i>Model: ${escapeHtml(prediction.modelUsed)}</small>
            </div>
            
            ${details.length > 0 ? `
                <div class="mt-2">
                    <strong class="small">Analysis Details:</strong>
                    <ul class="small mb-0 mt-1">
                        ${details.map(detail => `<li>${escapeHtml(detail)}</li>`).join('')}
                    </ul>
                </div>
            ` : ''}
            
            <div class="mt-3 text-end">
                <small class="text-muted">
                    <i class="far fa-clock me-1"></i>
                    ${formatTimeAgo(prediction.createdAt)}
                </small>
            </div>
        </div>
    `;
}