import { PolymerElement, html } from '@polymer/polymer/polymer-element.js';

class MainView extends PolymerElement {

    static get template() {
        return html`
            <style>
                :host {
                    display: block;
                    height: 100%;
                }
            </style>
            <slot></slot>
        `;
    }

    setText(text) {
        this.dispatchEvent(new CustomEvent('set-text', { detail: text }));
    }
}

customElements.define('main-view', MainView);